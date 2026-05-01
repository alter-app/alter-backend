package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.auth.vo.SocialAuthRequest;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginWithSocial 테스트")
class LoginWithSocialTests {

    @Mock
    private SocialAuthenticationManager socialAuthenticationManager;

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private AuthLogRepository authLogRepository;

    @InjectMocks
    private LoginWithSocial loginWithSocial;

    private SocialLoginRequestDto request;

    @BeforeEach
    void setUp() {
        request = new SocialLoginRequestDto(
            SocialProvider.KAKAO,
            null,
            "authCode",
            PlatformType.WEB
        );
    }

    private SocialAuthInfo createSocialAuthInfo() {
        SocialAuthInfo authInfo = mock(SocialAuthInfo.class);
        given(authInfo.getProvider()).willReturn(SocialProvider.KAKAO);
        given(authInfo.getSocialId()).willReturn("social-123");
        given(authInfo.getRefreshToken()).willReturn("refresh-token");
        return authInfo;
    }

    private SocialAuthInfo createSocialAuthInfoWithoutRefreshToken() {
        SocialAuthInfo authInfo = mock(SocialAuthInfo.class);
        given(authInfo.getProvider()).willReturn(SocialProvider.KAKAO);
        given(authInfo.getSocialId()).willReturn("social-123");
        return authInfo;
    }

    private UserSocial createMockUserSocial(User user) {
        UserSocial userSocial = mock(UserSocial.class);
        given(userSocial.getUser()).willReturn(user);
        return userSocial;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("소셜 계정이 존재하지 않을 경우 USER_NOT_FOUND 예외 발생")
        void fails_whenUserSocialNotFound() {
            // given
            SocialAuthInfo authInfo = createSocialAuthInfoWithoutRefreshToken();
            given(socialAuthenticationManager.authenticate(any(SocialAuthRequest.class))).willReturn(authInfo);
            given(userSocialQueryRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "social-123"
            )).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> loginWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });

            then(authService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("SUSPENDED 사용자 로그인 시 SUSPENDED_USER 예외 발생")
        void fails_whenUserIsSuspended() {
            // given
            SocialAuthInfo authInfo = createSocialAuthInfoWithoutRefreshToken();
            User user = mock(User.class);
            given(user.getStatus()).willReturn(UserStatus.SUSPENDED);
            UserSocial userSocial = createMockUserSocial(user);

            given(socialAuthenticationManager.authenticate(any(SocialAuthRequest.class))).willReturn(authInfo);
            given(userSocialQueryRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "social-123"
            )).willReturn(Optional.of(userSocial));

            // when & then
            assertThatThrownBy(() -> loginWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.SUSPENDED_USER);
                });

            then(authService).should(never()).revokeAllExistingAuthorizations(any());
            then(authService).should(never()).generateAuthorization(any(), any());
        }

        @Test
        @DisplayName("DELETED 사용자 로그인 시 DELETED_USER 예외 발생")
        void fails_whenUserIsDeleted() {
            // given
            SocialAuthInfo authInfo = createSocialAuthInfoWithoutRefreshToken();
            User user = mock(User.class);
            given(user.getStatus()).willReturn(UserStatus.DELETED);
            UserSocial userSocial = createMockUserSocial(user);

            given(socialAuthenticationManager.authenticate(any(SocialAuthRequest.class))).willReturn(authInfo);
            given(userSocialQueryRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "social-123"
            )).willReturn(Optional.of(userSocial));

            // when & then
            assertThatThrownBy(() -> loginWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.DELETED_USER);
                });

            then(authService).should(never()).revokeAllExistingAuthorizations(any());
            then(authService).should(never()).generateAuthorization(any(), any());
        }

        @Test
        @DisplayName("ACTIVE 사용자 로그인 성공")
        void succeeds_whenUserIsActive() {
            // given
            SocialAuthInfo authInfo = createSocialAuthInfo();
            User user = mock(User.class);
            given(user.getStatus()).willReturn(UserStatus.ACTIVE);
            given(user.getRole()).willReturn(UserRole.ROLE_USER);
            UserSocial userSocial = createMockUserSocial(user);
            Authorization authorization = mock(Authorization.class);

            given(socialAuthenticationManager.authenticate(any(SocialAuthRequest.class))).willReturn(authInfo);
            given(userSocialQueryRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "social-123"
            )).willReturn(Optional.of(userSocial));
            given(authService.generateAuthorization(user, TokenScope.APP)).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = loginWithSocial.execute(request);

            // then
            assertThat(result).isNotNull();
            then(userSocial).should().updateRefreshToken("refresh-token");
            then(authService).should().revokeAllExistingAuthorizations(user);
            then(authService).should().generateAuthorization(user, TokenScope.APP);
        }

        @Test
        @DisplayName("MANAGER 역할 사용자 로그인 시 MANAGER scope 토큰 발급")
        void succeeds_withManagerScope_whenUserIsManager() {
            // given
            SocialAuthInfo authInfo = createSocialAuthInfo();
            User user = mock(User.class);
            given(user.getStatus()).willReturn(UserStatus.ACTIVE);
            given(user.getRole()).willReturn(UserRole.ROLE_MANAGER);
            UserSocial userSocial = createMockUserSocial(user);
            Authorization authorization = mock(Authorization.class);

            given(socialAuthenticationManager.authenticate(any(SocialAuthRequest.class))).willReturn(authInfo);
            given(userSocialQueryRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "social-123"
            )).willReturn(Optional.of(userSocial));
            given(authService.generateAuthorization(user, TokenScope.MANAGER)).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = loginWithSocial.execute(request);

            // then
            assertThat(result).isNotNull();
            then(authService).should().generateAuthorization(user, TokenScope.MANAGER);
        }
    }
}
