package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.outbound.cache.SignupSessionCacheRepository;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserGender;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserWithSocial 테스트")
class CreateUserWithSocialTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @Mock
    private SocialAuthenticationManager socialAuthenticationManager;

    @Mock
    private AuthService authService;

    @Mock
    private AuthLogRepository authLogRepository;

    @Mock
    private SignupSessionCacheRepository cacheRepository;

    @InjectMocks
    private CreateUserWithSocial createUserWithSocial;

    private CreateUserWithSocialRequestDto request;

    @BeforeEach
    void setUp() {
        request = new CreateUserWithSocialRequestDto(
            "signup-session-id",
            SocialProvider.KAKAO,
            null,
            "auth-code",
            PlatformType.WEB,
            "김철수",
            "유땡땡",
            UserGender.GENDER_MALE,
            "19900101"
        );
    }

    private SocialAuthInfo createSocialAuthInfo(String socialId, String email, String refreshToken) {
        SocialAuthInfo authInfo = mock(SocialAuthInfo.class);
        given(authInfo.getProvider()).willReturn(SocialProvider.KAKAO);
        given(authInfo.getSocialId()).willReturn(socialId);
        if (email != null) given(authInfo.getEmail()).willReturn(email);
        if (refreshToken != null) given(authInfo.getRefreshToken()).willReturn(refreshToken);
        return authInfo;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("회원가입 세션이 존재하지 않을 경우 SIGNUP_SESSION_NOT_EXIST 예외 발생")
        void execute_signupSessionNotFound_throwsSignupSessionNotExist() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SIGNUP_SESSION_NOT_EXIST));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("닉네임이 중복될 경우 NICKNAME_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void execute_nicknameDuplicated_throwsNicknameDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NICKNAME_DUPLICATED));

            then(cacheRepository).should().deleteAll(anyList());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("연락처가 중복될 경우 USER_CONTACT_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void execute_contactDuplicated_throwsUserContactDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_CONTACT_DUPLICATED));

            then(cacheRepository).should().deleteAll(anyList());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 등록된 소셜 계정일 경우 SOCIAL_ID_DUPLICATED 예외 발생")
        void execute_socialIdAlreadyRegistered_throwsSocialIdDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo("kakao-social-id", null, null);
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SOCIAL_ID_DUPLICATED));

            then(cacheRepository).should(never()).deleteAll(anyList());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("소셜 계정 이메일이 이미 가입된 경우 EMAIL_DUPLICATED 예외 발생")
        void execute_socialEmailAlreadyExists_throwsEmailDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo("kakao-social-id", "social@example.com", null);
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(false);
            given(userQueryRepository.findByEmail("social@example.com")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.EMAIL_DUPLICATED));

            then(cacheRepository).should(never()).deleteAll(anyList());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("유효한 입력으로 소셜 회원가입 성공 - 소셜 계정 이메일 자동 저장")
        void execute_withValidInput_succeeds() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo("kakao-social-id", "social@example.com", "kakao-refresh-token");
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(false);
            given(userQueryRepository.findByEmail("social@example.com")).willReturn(Optional.empty());

            User savedUser = mock(User.class);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            Authorization authorization = mock(Authorization.class);
            given(authService.generateAuthorization(eq(savedUser), eq(TokenScope.APP))).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = createUserWithSocial.execute(request);

            // then
            assertThat(result).isNotNull();
            then(userRepository).should().save(any(User.class));
            then(userQueryRepository).should().findByEmail("social@example.com");
            then(authService).should().generateAuthorization(savedUser, TokenScope.APP);
            then(authLogRepository).should().save(any());
            then(cacheRepository).should().deleteAll(anyList());
        }

        @Test
        @DisplayName("소셜 계정에 이메일이 없을 경우 이메일 없이 회원가입 성공")
        void execute_withNoEmailFromSocial_succeeds() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo("kakao-social-id", null, "kakao-refresh-token");
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(false);

            User savedUser = mock(User.class);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            Authorization authorization = mock(Authorization.class);
            given(authService.generateAuthorization(eq(savedUser), eq(TokenScope.APP))).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = createUserWithSocial.execute(request);

            // then
            assertThat(result).isNotNull();
            then(userRepository).should().save(any(User.class));
            then(userQueryRepository).should(never()).findByEmail(any());
            then(cacheRepository).should().deleteAll(anyList());
        }
    }
}
