package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApproveJoinRequest 테스트")
class ApproveJoinRequestTests {

    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CreateWorkspaceWorkerUseCase addWorkerToWorkspace;

    @InjectMocks
    private ApproveJoinRequest approveJoinRequest;

    private ManagerActor actor;
    private ManagerUser managerUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        managerUser = mock(ManagerUser.class);
        actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(managerUser);

        workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(1L);
        given(workspace.getBusinessName()).willReturn("테스트업장");
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
            assertThatThrownBy(() -> approveJoinRequest.execute(actor, 1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("해당 업장의 관리자가 아니면 FORBIDDEN 예외 발생")
        void fails_whenNotWorkspaceManager() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> approveJoinRequest.execute(actor, 1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 합류 요청 ID이면 NOT_FOUND 예외 발생")
        void fails_whenJoinRequestNotFound() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(businessJoinRequestQueryRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> approveJoinRequest.execute(actor, 1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("다른 업장의 합류 요청이면 FORBIDDEN 예외 발생")
        void fails_whenJoinRequestBelongsToDifferentWorkspace() {
            // given
            Workspace otherWorkspace = mock(Workspace.class);
            given(otherWorkspace.getId()).willReturn(999L); // workspaceId=1L과 다름

            BusinessJoinRequest joinRequest = mock(BusinessJoinRequest.class);
            given(joinRequest.getWorkspace()).willReturn(otherWorkspace);

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(businessJoinRequestQueryRepository.findById(10L)).willReturn(Optional.of(joinRequest));

            // when & then
            assertThatThrownBy(() -> approveJoinRequest.execute(actor, 1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("이미 처리된 합류 요청을 승인하면 CONFLICT 예외 발생")
        void fails_whenJoinRequestAlreadyProcessed() {
            // given
            User requester = mock(User.class);
            given(requester.getId()).willReturn(50L);

            BusinessJoinRequest joinRequest = mock(BusinessJoinRequest.class);
            given(joinRequest.getWorkspace()).willReturn(workspace);
            given(joinRequest.getUser()).willReturn(requester);
            willThrow(new CustomException(ErrorCode.CONFLICT, "승인할 수 없는 상태의 합류 요청입니다."))
                .given(joinRequest).approve();

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(businessJoinRequestQueryRepository.findById(10L)).willReturn(Optional.of(joinRequest));

            // when & then
            assertThatThrownBy(() -> approveJoinRequest.execute(actor, 1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("정상 승인 시 워커 추가 및 FCM 이벤트 발행")
        void succeeds_addsWorkerAndPublishesEvent() {
            // given
            User requester = mock(User.class);
            given(requester.getId()).willReturn(50L);

            BusinessJoinRequest joinRequest = mock(BusinessJoinRequest.class);
            given(joinRequest.getWorkspace()).willReturn(workspace);
            given(joinRequest.getUser()).willReturn(requester);

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(businessJoinRequestQueryRepository.findById(10L)).willReturn(Optional.of(joinRequest));

            // when
            approveJoinRequest.execute(actor, 1L, 10L);

            // then
            then(joinRequest).should().approve();
            then(addWorkerToWorkspace).should().execute(workspace, requester);
            then(eventPublisher).should().publishEvent(any(FcmNotificationEvent.class));
        }
    }
}
