package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeclineWorkspaceInvitation 테스트")
class DeclineWorkspaceInvitationTests {

    @Mock private BusinessInvitationQueryRepository businessInvitationQueryRepository;

    @InjectMocks
    private DeclineWorkspaceInvitation declineWorkspaceInvitation;

    private AppActor actor;

    @BeforeEach
    void setUp() {
        actor = mock(AppActor.class);
        given(actor.getUserId()).willReturn(1L);
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
            assertThatThrownBy(() -> declineWorkspaceInvitation.execute(actor, 99L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        }

        @Test
        @DisplayName("초대 수신자가 아닌 유저가 거절하면 FORBIDDEN 예외 발생")
        void fails_whenNotInvitedUser() {
            // given
            User anotherUser = mock(User.class);
            given(anotherUser.getId()).willReturn(999L);

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(anotherUser);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));

            // when & then
            assertThatThrownBy(() -> declineWorkspaceInvitation.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

            then(invitation).should(org.mockito.Mockito.never()).decline();
        }

        @Test
        @DisplayName("이미 처리된 초대(ACCEPTED/EXPIRED)를 거절하면 CONFLICT 예외 발생")
        void fails_whenInvitationAlreadyProcessed() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(1L);

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(invitedUser);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));
            willThrow(new CustomException(ErrorCode.CONFLICT, "거절할 수 없는 상태의 초대입니다."))
                .given(invitation).decline();

            // when & then
            assertThatThrownBy(() -> declineWorkspaceInvitation.execute(actor, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        }

        @Test
        @DisplayName("정상 거절 시 decline() 호출")
        void succeeds_callsDecline() {
            // given
            User invitedUser = mock(User.class);
            given(invitedUser.getId()).willReturn(1L);

            BusinessInvitation invitation = mock(BusinessInvitation.class);
            given(invitation.getInvitedUser()).willReturn(invitedUser);
            given(businessInvitationQueryRepository.findById(1L)).willReturn(Optional.of(invitation));

            // when
            declineWorkspaceInvitation.execute(actor, 1L);

            // then
            then(invitation).should().decline();
        }
    }
}
