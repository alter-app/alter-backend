package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AcceptWorkspaceInvitation 테스트")
class AcceptWorkspaceInvitationTests {

    @Mock private BusinessInvitationQueryRepository businessInvitationQueryRepository;
    @Mock private CreateWorkspaceWorkerUseCase addWorkerToWorkspace;

    @InjectMocks
    private AcceptWorkspaceInvitation acceptWorkspaceInvitation;

    private AppActor actor;
    private User actorUser;

    @BeforeEach
    void setUp() {
        actorUser = mock(User.class);
        actor = mock(AppActor.class);
        given(actor.getUserId()).willReturn(1L);
        given(actor.getUser()).willReturn(actorUser);
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("존재하지 않는 초대 ID이면 NOT_FOUND 예외 발생")
        void fails_whenInvitationNotFound() {
            // given
            given(businessInvitationQueryRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> acceptWorkspaceInvitation.execute(actor, 99L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("초대 수신자가 아닌 유저가 수락하면 FORBIDDEN 예외 발생")
        void fails_whenNotInvitedUser() {
            // given
            User anotherUser = mock(User.class);
            given(anotherUser.getId()).willReturn(999L); // actor userId=1L과 다름

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(anotherUser);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));

            // when & then
            assertThatThrownBy(() -> acceptWorkspaceInvitation.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("만료된 초대를 수락하면 CONFLICT 예외 발생")
        void fails_whenInvitationExpired() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(1L);

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(invitedUser);
            given(invitation.isExpired()).willReturn(true);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));

            // when & then
            assertThatThrownBy(() -> acceptWorkspaceInvitation.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("이미 처리된 초대(ACCEPTED/DECLINED)를 수락하면 CONFLICT 예외 발생")
        void fails_whenInvitationAlreadyProcessed() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(1L);

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(invitedUser);
            given(invitation.isExpired()).willReturn(false);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));
            // accept()가 이미 처리된 상태라 내부에서 예외 발생
            org.mockito.BDDMockito.willThrow(new CustomException(ErrorCode.CONFLICT, "수락할 수 없는 상태의 초대입니다."))
                .given(invitation).accept();

            // when & then
            assertThatThrownBy(() -> acceptWorkspaceInvitation.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

            then(addWorkerToWorkspace).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("정상 수락 시 워커 추가 UseCase 호출")
        void succeeds_addsWorkerToWorkspace() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(1L);

            Workspace workspace = mock(Workspace.class);
            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(invitedUser);
            given(invitation.isExpired()).willReturn(false);
            given(invitation.getWorkspace()).willReturn(workspace);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));

            // when
            acceptWorkspaceInvitation.execute(actor, 1L);

            // then
            then(invitation).should().accept();
            then(addWorkerToWorkspace).should().execute(workspace, actorUser);
        }
    }

    // ArgumentCaptor 사용을 위한 any() 헬퍼 import 회피
    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
