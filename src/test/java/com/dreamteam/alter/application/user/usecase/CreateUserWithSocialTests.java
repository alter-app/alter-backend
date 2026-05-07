package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.terms.service.TermsAgreementValidator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.type.TermsType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserWithSocial 테스트")
class CreateUserWithSocialTests {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @Mock
    private SocialAuthenticationManager socialAuthenticationManager;

    @Mock
    private SignupSessionCacheRepository cacheRepository;

    @Mock
    private CreateUserWithSocialTx createUserWithSocialTx;

    @Mock
    private TermsAgreementValidator termsAgreementValidator;

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
            "19900101",
            true,
            false,
            Set.of(TermsType.SERVICE, TermsType.PRIVACY)
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

            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
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
            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
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
            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("이미 등록된 소셜 계정일 경우 SOCIAL_ID_DUPLICATED 예외 발생")
        void execute_socialIdAlreadyRegistered_throwsSocialIdDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            given(termsAgreementValidator.validateAndResolve(any())).willReturn(List.of());

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
            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("소셜 계정 이메일이 이미 가입된 경우 EMAIL_DUPLICATED 예외 발생")
        void execute_socialEmailAlreadyExists_throwsEmailDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            given(termsAgreementValidator.validateAndResolve(any())).willReturn(List.of());

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
            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외 발생")
        void execute_requiredTermsNotAgreed_throwsRequiredTermsNotAgreed() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            willThrow(new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED))
                .given(termsAgreementValidator).validateAndResolve(any());

            // when & then
            assertThatThrownBy(() -> createUserWithSocial.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED));

            then(socialAuthenticationManager).should(never()).authenticate(any());
            then(createUserWithSocialTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("유효한 입력으로 소셜 회원가입 성공")
        void execute_withValidInput_succeeds() {
            // given - 기본 세션 및 검증
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            given(termsAgreementValidator.validateAndResolve(any())).willReturn(List.of());

            // 소셜 인증 및 중복 확인
            SocialAuthInfo authInfo = createSocialAuthInfo("kakao-social-id", null, null);
            given(socialAuthenticationManager.authenticate(any())).willReturn(authInfo);
            given(userSocialQueryRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, "kakao-social-id")).willReturn(false);

            // TX 저장
            GenerateTokenResponseDto mockResponse = mock(GenerateTokenResponseDto.class);
            given(createUserWithSocialTx.process(any(), any(), any(), eq(true), eq(false), anyList())).willReturn(mockResponse);

            // when
            GenerateTokenResponseDto result = createUserWithSocial.execute(request);

            // then
            assertThat(result).isEqualTo(mockResponse);
            then(createUserWithSocialTx).should().process(eq("01012345678"), eq(request), any(), eq(true), eq(false), anyList());
            then(cacheRepository).should().deleteAll(anyList());
        }
    }
}
