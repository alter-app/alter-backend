package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private StringRedisTemplate redisTemplate;

    @Mock
    private EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;

    @InjectMocks
    private CreateUserWithSocial createUserWithSocial;

    @Mock
    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations;

    private CreateUserWithSocialRequestDto request;

    @BeforeEach
    void setUp() {
        request = new CreateUserWithSocialRequestDto(
            "signup-session-id",
            null,
            SocialProvider.KAKAO,
            null,
            "auth-code",
            PlatformType.WEB,
            "김철수",
            "유땡땡",
            UserGender.GENDER_MALE,
            "19900101"
        );

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    private SocialAuthInfo createSocialAuthInfo() {
        SocialAuthInfo authInfo = mock(SocialAuthInfo.class);
        given(authInfo.getProvider()).willReturn(SocialProvider.KAKAO);
        given(authInfo.getSocialId()).willReturn("kakao-social-id");
        given(authInfo.getRefreshToken()).willReturn("kakao-refresh-token");
        return authInfo;
    }

    private SocialAuthInfo createSocialAuthInfoWithoutToken() {
        SocialAuthInfo authInfo = mock(SocialAuthInfo.class);
        given(authInfo.getProvider()).willReturn(SocialProvider.KAKAO);
        given(authInfo.getSocialId()).willReturn("kakao-social-id");
        return authInfo;
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("회원가입 세션이 존재하지 않을 경우 SIGNUP_SESSION_NOT_EXIST 예외 발생")
        void fails_whenSignupSessionNotFound() {
            // given
            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SIGNUP_SESSION_NOT_EXIST));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("닉네임이 중복될 경우 NICKNAME_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void fails_whenNicknameDuplicated() {
            // given
            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NICKNAME_DUPLICATED));

            then(redisTemplate).should().delete("SIGNUP:PENDING:signup-session-id");
            then(redisTemplate).should().delete("SIGNUP:CONTACT:01012345678");
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("연락처가 중복될 경우 USER_CONTACT_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void fails_whenContactDuplicated() {
            // given
            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_CONTACT_DUPLICATED));

            then(redisTemplate).should().delete("SIGNUP:PENDING:signup-session-id");
            then(redisTemplate).should().delete("SIGNUP:CONTACT:01012345678");
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 등록된 소셜 계정일 경우 SOCIAL_ID_DUPLICATED 예외 발생")
        void fails_whenSocialIdAlreadyRegistered() {
            // given
            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfoWithoutToken();
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SOCIAL_ID_DUPLICATED));

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("유효한 입력으로 소셜 회원가입 성공")
        void succeeds_withValidSocialSignup() {
            // given
            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo();
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
            then(authService).should().generateAuthorization(savedUser, TokenScope.APP);
            then(authLogRepository).should().save(any());
            then(redisTemplate).should().delete("SIGNUP:PENDING:signup-session-id");
            then(redisTemplate).should().delete("SIGNUP:CONTACT:01012345678");
        }

        @Test
        @DisplayName("이메일 세션이 제공된 경우 이메일 인증 처리 및 세션 삭제")
        void succeeds_withEmailSessionProvided() {
            // given
            CreateUserWithSocialRequestDto requestWithEmail = new CreateUserWithSocialRequestDto(
                "signup-session-id",
                "email-session-id",
                SocialProvider.KAKAO,
                null,
                "auth-code",
                PlatformType.WEB,
                "김철수",
                "유땡땡",
                UserGender.GENDER_MALE,
                "19900101"
            );

            given(valueOperations.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());

            SocialAuthInfo authInfo = createSocialAuthInfo();
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id"))
                .willReturn(false);

            given(emailVerificationSessionStoreRepository.getEmailBySession("email-session-id"))
                .willReturn(Optional.of("test@example.com"));
            given(userQueryRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

            User savedUser = mock(User.class);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            Authorization authorization = mock(Authorization.class);
            given(authService.generateAuthorization(eq(savedUser), eq(TokenScope.APP))).willReturn(authorization);

            // when
            GenerateTokenResponseDto result = createUserWithSocial.execute(requestWithEmail);

            // then
            assertThat(result).isNotNull();
            then(emailVerificationSessionStoreRepository).should().getEmailBySession("email-session-id");
            then(emailVerificationSessionStoreRepository).should().deleteSession("email-session-id");
        }
    }
}
