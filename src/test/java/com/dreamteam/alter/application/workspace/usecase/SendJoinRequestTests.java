package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SendJoinRequest 테스트")
class SendJoinRequestTests {

    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    @Mock private BusinessJoinRequestRepository businessJoinRequestRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SendJoinRequest sendJoinRequest;

    private AppActor actor;
    private User actorUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        actorUser = mock(User.class);
        given(actorUser.getId()).willReturn(1L);
        given(actorUser.getName()).willReturn("테스트유저");

        actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(actorUser);

        User managerUser_user = mock(User.class);
        given(managerUser_user.getId()).willReturn(100L);

        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(managerUser_user);

        workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(1L);
        given(workspace.getBusinessName()).willReturn("테스트업장");
        given(workspace.getManagerUser()).willReturn(managerUser);
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("업장이 존재하지 않으면 WORKSPACE_NOT_FOUND 예외 발생")
        void fails_whenWorkspaceNotFound() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sendJoinRequest.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));

            then(businessJoinRequestRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 활성 워커인 사용자가 요청하면 WORKSPACE_WORKER_ALREADY_EXISTS 예외 발생")
        void fails_whenUserIsAlreadyActiveWorker() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.isUserActiveWorkerInWorkspace(actorUser, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sendJoinRequest.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS));

            then(businessJoinRequestRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 대기 중인 합류 요청이 있으면 CONFLICT 예외 발생")
        void fails_whenPendingRequestExists() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.isUserActiveWorkerInWorkspace(actorUser, 1L)).willReturn(false);
            given(businessJoinRequestQueryRepository.existsPendingRequest(workspace, actorUser)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sendJoinRequest.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            then(businessJoinRequestRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("정상 합류 요청 시 요청 저장 및 FCM 이벤트 발행")
        void succeeds_savesRequestAndPublishesEvent() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.isUserActiveWorkerInWorkspace(actorUser, 1L)).willReturn(false);
            given(businessJoinRequestQueryRepository.existsPendingRequest(workspace, actorUser)).willReturn(false);

            // when
            sendJoinRequest.execute(actor, 1L);

            // then
            then(businessJoinRequestRepository).should().save(any());
            then(eventPublisher).should().publishEvent(any(FcmNotificationEvent.class));
        }
    }
}
