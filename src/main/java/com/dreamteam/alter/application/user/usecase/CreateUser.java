package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.common.constants.SignupSessionConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.application.terms.service.TermsAgreementValidator;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service("createUser")
@RequiredArgsConstructor
public class CreateUser implements CreateUserUseCase {

    private final UserQueryRepository userQueryRepository;
    private final SignupSessionCacheRepository cacheRepository;
    private final EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;
    private final CreateUserTx createUserTx;
    private final TermsAgreementValidator termsAgreementValidator;

    @Override
    public GenerateTokenResponseDto execute(CreateUserRequestDto request) {

        // Redis 세션에서 휴대폰 인증 정보 확인
        String sessionIdKey = SignupSessionConstants.Session.KEY_PREFIX + request.getSignupSessionId();
        String contact = cacheRepository.get(sessionIdKey);

        if (ObjectUtils.isEmpty(contact)) {
            throw new CustomException(ErrorCode.SIGNUP_SESSION_NOT_EXIST);
        }

        // 중복 확인
        validateDuplication(request, contact, sessionIdKey);

        // 약관 동의 검증
        List<Terms> agreedTerms = termsAgreementValidator.validateAndResolve(request.getAgreedTermsTypes());

        // 비밀번호 형식 검증
        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 이메일 인증 세션 검증 (선택)
        String verifiedEmail = resolveVerifiedEmail(request);

        // 사용자 엔티티 저장
        GenerateTokenResponseDto response = createUserTx.process(
            request, contact, verifiedEmail,
            request.getNotificationConsent(),
            request.getNightNotificationConsent(),
            agreedTerms
        );

        // DB 트랜잭션 커밋 후 외부 인증 세션 정리
        cleanupSessions(request, sessionIdKey, contact, verifiedEmail);

        return response;
    }

    private void cleanupSessions(
        CreateUserRequestDto request,
        String sessionIdKey,
        String contact,
        String verifiedEmail
    ) {
        String contactKey = SignupSessionConstants.Session.CONTACT_INDEX_KEY_PREFIX + contact;
        try {
            cacheRepository.deleteAll(Arrays.asList(sessionIdKey, contactKey));
        } catch (Exception e) {
            log.error("회원가입 완료 후 회원가입 세션 삭제 실패", e);
        }

        if (ObjectUtils.isEmpty(verifiedEmail)) {
            return;
        }

        try {
            emailVerificationSessionStoreRepository.deleteSession(request.getEmailSessionId());
        } catch (Exception e) {
            log.error("회원가입 완료 후 이메일 인증 세션 삭제 실패", e);
        }
    }

    private String resolveVerifiedEmail(CreateUserRequestDto request) {
        String emailSessionId = request.getEmailSessionId();
        if (ObjectUtils.isEmpty(emailSessionId)) {
            return null;
        }

        String verifiedEmail = emailVerificationSessionStoreRepository
                .getEmailBySession(emailSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이메일 인증 세션이 유효하지 않거나 만료되었습니다."));

        userQueryRepository.findByEmail(verifiedEmail)
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
                });

        return verifiedEmail;
    }

    private void validateDuplication(CreateUserRequestDto request, String contact, String sessionIdKey) {
        String contactKey = SignupSessionConstants.Session.CONTACT_INDEX_KEY_PREFIX + contact;

        // 닉네임 중복 확인
        if (userQueryRepository.findByNickname(request.getNickname()).isPresent()) {
            cacheRepository.deleteAll(Arrays.asList(sessionIdKey, contactKey));
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 연락처 중복 확인
        if (userQueryRepository.findByContact(contact).isPresent()) {
            cacheRepository.deleteAll(Arrays.asList(sessionIdKey, contactKey));
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }
    }
}
