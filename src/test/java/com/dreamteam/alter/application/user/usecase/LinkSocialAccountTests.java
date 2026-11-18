package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.LinkSocialAccountCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkSocialAccount 테스트")
class LinkSocialAccountTests {

    @Mock
    private SocialAuthenticationManager socialAuthenticationManager;

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @Mock
    private User user;

    @InjectMocks
    private LinkSocialAccount linkSocialAccount;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("소셜 인증이 실패하면 예외를 그대로 전파하고 소셜 계정을 연동하지 않는다")
        void execute_socialAuthFails_throwsWithoutLinkingAccount() {
            // given
            LinkSocialAccountCommand command = LinkSocialAccountCommand.of(
                user, SocialProvider.KAKAO, null, "auth-code", PlatformType.WEB);
            willThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "소셜 계정 인증 처리 중 오류가 발생했습니다."))
                .given(socialAuthenticationManager).authenticate(any());

            // when & then
            assertThatThrownBy(() -> linkSocialAccount.execute(command))
                .isInstanceOfSatisfying(CustomException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getMessage()).isEqualTo("소셜 계정 인증 처리 중 오류가 발생했습니다.");
                });

            then(userSocialQueryRepository).shouldHaveNoInteractions();
            then(user).shouldHaveNoInteractions();
        }
    }
}
