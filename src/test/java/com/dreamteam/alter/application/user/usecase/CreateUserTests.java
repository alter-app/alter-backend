package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.application.terms.service.TermsAgreementValidator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUser 테스트")
class CreateUserTests {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private SignupSessionCacheRepository cacheRepository;

    @Mock
    private EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;

    @Mock
    private CreateUserTx createUserTx;

    @Mock
    private TermsAgreementValidator termsAgreementValidator;

    @InjectMocks
    private CreateUser createUser;

    private CreateUserRequestDto request;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequestDto(
            "signup-session-id",
            null,
            "Test1234!",
            "김철수",
            "유땡땡",
            UserGender.GENDER_MALE,
            "19900101",
            true,
            false,
            Set.of(TermsType.SERVICE, TermsType.PRIVACY)
        );
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
            assertThatThrownBy(() -> createUser.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.SIGNUP_SESSION_NOT_EXIST));

            then(createUserTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("닉네임이 중복될 경우 NICKNAME_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void execute_nicknameDuplicated_throwsNicknameDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUser.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NICKNAME_DUPLICATED));

            then(cacheRepository).should().deleteAll(argThat(list ->
                list.containsAll(List.of("SIGNUP:PENDING:signup-session-id", "SIGNUP:CONTACT:01012345678"))));
            then(createUserTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("연락처가 중복될 경우 USER_CONTACT_DUPLICATED 예외 발생 및 Redis 세션 삭제")
        void execute_contactDuplicated_throwsUserContactDuplicated() {
            // given
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.of(mock(User.class)));

            // when & then
            assertThatThrownBy(() -> createUser.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_CONTACT_DUPLICATED));

            then(cacheRepository).should().deleteAll(argThat(list ->
                list.containsAll(List.of("SIGNUP:PENDING:signup-session-id", "SIGNUP:CONTACT:01012345678"))));
            then(createUserTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
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
            assertThatThrownBy(() -> createUser.execute(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED));

            then(createUserTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("비밀번호 형식이 잘못된 경우 INVALID_PASSWORD_FORMAT 예외 발생")
        void execute_invalidPasswordFormat_throwsInvalidPasswordFormat() {
            // given
            CreateUserRequestDto invalidRequest = new CreateUserRequestDto(
                "signup-session-id",
                null,
                "password123",
                "김철수",
                "유땡땡",
                UserGender.GENDER_MALE,
                "19900101",
                true,
                false,
                Set.of(TermsType.SERVICE, TermsType.PRIVACY)
            );
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            given(termsAgreementValidator.validateAndResolve(any())).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> createUser.execute(invalidRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_PASSWORD_FORMAT));

            then(createUserTx).should(never()).process(any(), any(), any(), anyBoolean(), anyBoolean(), anyList());
        }

        @Test
        @DisplayName("유효한 입력으로 일반 회원가입 성공")
        void execute_withValidInput_succeeds() {
            // given
            Terms termsMock1 = mock(Terms.class);
            Terms termsMock2 = mock(Terms.class);
            List<Terms> agreedTerms = List.of(termsMock1, termsMock2);
            given(cacheRepository.get("SIGNUP:PENDING:signup-session-id")).willReturn("01012345678");
            given(userQueryRepository.findByNickname("유땡땡")).willReturn(Optional.empty());
            given(userQueryRepository.findByContact("01012345678")).willReturn(Optional.empty());
            given(termsAgreementValidator.validateAndResolve(any())).willReturn(agreedTerms);

            GenerateTokenResponseDto mockResponse = mock(GenerateTokenResponseDto.class);
            given(createUserTx.process(any(), any(), any(), anyBoolean(), anyBoolean(), eq(agreedTerms))).willReturn(mockResponse);

            // when
            GenerateTokenResponseDto result = createUser.execute(request);

            // then
            assertThat(result).isEqualTo(mockResponse);
            then(createUserTx).should().process(eq(request), eq("01012345678"), any(), eq(true), eq(false), eq(agreedTerms));
            then(cacheRepository).should().deleteAll(argThat(list ->
                list.containsAll(List.of("SIGNUP:PENDING:signup-session-id", "SIGNUP:CONTACT:01012345678"))));
            then(emailVerificationSessionStoreRepository).should(never()).deleteSession(any());
        }
    }
}
