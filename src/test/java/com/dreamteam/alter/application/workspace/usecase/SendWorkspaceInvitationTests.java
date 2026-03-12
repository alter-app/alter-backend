package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.exception.InvitationUnavailableException;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SendWorkspaceInvitation 테스트")
class SendWorkspaceInvitationTests {

    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private UserQueryRepository userQueryRepository;
    @Mock private BusinessInvitationRepository businessInvitationRepository;
    @Mock private BusinessInvitationQueryRepository businessInvitationQueryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SendWorkspaceInvitation sendWorkspaceInvitation;

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

    private SendWorkspaceInvitationRequestDto requestOf(Set<String> phoneNumbers) {
        SendWorkspaceInvitationRequestDto dto = new SendWorkspaceInvitationRequestDto();
        ReflectionTestUtils.setField(dto, "phoneNumbers", phoneNumbers);
        return dto;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("업장이 존재하지 않으면 WORKSPACE_NOT_FOUND 예외 발생")
        void fails_whenWorkspaceNotFound() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.empty());
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01011111111"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));

            then(businessInvitationRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("해당 업장의 관리자가 아니면 FORBIDDEN 예외 발생")
        void fails_whenNotWorkspaceManager() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(false);
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01011111111"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(businessInvitationRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("앱에 가입되지 않은 번호가 포함되면 InvitationUnavailableException 발생")
        void fails_whenPhoneNumberNotRegistered() {
            // given
            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(userQueryRepository.findByContactIn(Set.of("01099999999"))).willReturn(List.of());
            given(workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(any(), any())).willReturn(Set.of());
            given(businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(any(), any())).willReturn(Set.of());
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01099999999"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(InvitationUnavailableException.class)
                .satisfies(ex -> {
                    InvitationUnavailableException invEx = (InvitationUnavailableException) ex;
                    assertThat(invEx.getUnavailablePhoneNumbers()).containsExactly("01099999999");
                });

            then(businessInvitationRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("이미 활성 워커인 사용자 번호가 포함되면 InvitationUnavailableException 발생")
        void fails_whenUserIsAlreadyActiveWorker() {
            // given
            User activeWorker = mock(User.class);
            given(activeWorker.getId()).willReturn(10L);
            given(activeWorker.getContact()).willReturn("01011111111");

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(userQueryRepository.findByContactIn(Set.of("01011111111"))).willReturn(List.of(activeWorker));
            given(workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(1L, Set.of(10L))).willReturn(Set.of(10L));
            given(businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(1L, Set.of(10L))).willReturn(Set.of());
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01011111111"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(InvitationUnavailableException.class)
                .satisfies(ex -> {
                    InvitationUnavailableException invEx = (InvitationUnavailableException) ex;
                    assertThat(invEx.getUnavailablePhoneNumbers()).containsExactly("01011111111");
                });

            then(businessInvitationRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("이미 대기 중인 초대가 있는 사용자 번호가 포함되면 InvitationUnavailableException 발생")
        void fails_whenPendingInvitationExists() {
            // given
            User pendingUser = mock(User.class);
            given(pendingUser.getId()).willReturn(20L);
            given(pendingUser.getContact()).willReturn("01022222222");

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(userQueryRepository.findByContactIn(Set.of("01022222222"))).willReturn(List.of(pendingUser));
            given(workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(1L, Set.of(20L))).willReturn(Set.of());
            given(businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(1L, Set.of(20L))).willReturn(Set.of(20L));
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01022222222"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(InvitationUnavailableException.class)
                .satisfies(ex -> {
                    InvitationUnavailableException invEx = (InvitationUnavailableException) ex;
                    assertThat(invEx.getUnavailablePhoneNumbers()).containsExactly("01022222222");
                });
        }

        @Test
        @DisplayName("정상 초대 발송 시 초대 저장 및 FCM 이벤트 발행")
        void succeeds_savesInvitationsAndPublishesEvents() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(30L);
            given(invitedUser.getContact()).willReturn("01033333333");

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            given(userQueryRepository.findByContactIn(Set.of("01033333333"))).willReturn(List.of(invitedUser));
            given(workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(1L, Set.of(30L))).willReturn(Set.of());
            given(businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(1L, Set.of(30L))).willReturn(Set.of());
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01033333333"));

            // when
            sendWorkspaceInvitation.execute(actor, 1L, request);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<BusinessInvitation>> captor = ArgumentCaptor.forClass(List.class);
            then(businessInvitationRepository).should().saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            then(eventPublisher).should().publishEvent(any(FcmNotificationEvent.class));
        }

        @Test
        @DisplayName("여러 번호 중 하나라도 불가 번호가 있으면 전체 예외 발생 후 저장 없음")
        void fails_whenAnyPhoneNumberIsUnavailable() {
            // given
            User validUser = mock(User.class);
            given(validUser.getId()).willReturn(40L);
            given(validUser.getContact()).willReturn("01044444444");

            given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
            given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
            // 01099999999는 미가입 → contactToUser에서 조회 안 됨
            given(userQueryRepository.findByContactIn(any())).willReturn(List.of(validUser));
            given(workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(any(), any())).willReturn(Set.of());
            given(businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(any(), any())).willReturn(Set.of());
            SendWorkspaceInvitationRequestDto request = requestOf(Set.of("01044444444", "01099999999"));

            // when & then
            assertThatThrownBy(() -> sendWorkspaceInvitation.execute(actor, 1L, request))
                .isInstanceOf(InvitationUnavailableException.class)
                .satisfies(ex -> {
                    InvitationUnavailableException invEx = (InvitationUnavailableException) ex;
                    assertThat(invEx.getUnavailablePhoneNumbers()).containsExactly("01099999999");
                });

            then(businessInvitationRepository).should(never()).saveAll(any());
        }
    }
}
