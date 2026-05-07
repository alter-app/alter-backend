package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.terms.service.TermsAgreementValidator;
import com.dreamteam.alter.common.constants.SignupSessionConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.vo.SocialAuthRequest;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserWithSocialUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.vo.OauthToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service("createUserWithSocial")
@RequiredArgsConstructor
public class CreateUserWithSocial implements CreateUserWithSocialUseCase {

    private final UserQueryRepository userQueryRepository;
    private final UserSocialQueryRepository userSocialQueryRepository;
    private final SocialAuthenticationManager socialAuthenticationManager;
    private final SignupSessionCacheRepository cacheRepository;
    private final CreateUserWithSocialTx createUserWithSocialTx;
    private final TermsAgreementValidator termsAgreementValidator;

    @Override
    public GenerateTokenResponseDto execute(CreateUserWithSocialRequestDto request) {

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

        // 소셜 인증
        OauthToken oauthToken = request.getOauthToken() != null
            ? OauthToken.of(request.getOauthToken().getAccessToken(), request.getOauthToken().getRefreshToken())
            : null;
        SocialAuthRequest socialAuthRequest = new SocialAuthRequest(
            request.getProvider(),
            oauthToken,
            request.getAuthorizationCode(),
            request.getPlatformType()
        );
        SocialAuthInfo socialAuthInfo = socialAuthenticationManager.authenticate(socialAuthRequest);

        // 이미 연동된 소셜 계정인지 확인
        if (userSocialQueryRepository.existsBySocialProviderAndSocialId(
            socialAuthInfo.getProvider(), socialAuthInfo.getSocialId()
        )) {
            throw new CustomException(ErrorCode.SOCIAL_ID_DUPLICATED);
        }

        // 소셜 계정 이메일 중복 확인
        String email = socialAuthInfo.getEmail();
        if (ObjectUtils.isNotEmpty(email)) {
            userQueryRepository.findByEmail(email)
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
                });
        }

        // 사용자 및 소셜 계정 엔티티 저장
        GenerateTokenResponseDto response = createUserWithSocialTx.process(
            contact, request, socialAuthInfo,
            request.getNotificationConsent(),
            request.getNightNotificationConsent(),
            agreedTerms
        );

        // 회원가입 세션 삭제
        String contactKey = SignupSessionConstants.Session.CONTACT_INDEX_KEY_PREFIX + contact;
        cacheRepository.deleteAll(Arrays.asList(sessionIdKey, contactKey));

        return response;
    }

    private void validateDuplication(CreateUserWithSocialRequestDto request, String contact, String sessionIdKey) {
        String contactKey = SignupSessionConstants.Session.CONTACT_INDEX_KEY_PREFIX + contact;
        List<String> keysToDelete = Arrays.asList(sessionIdKey, contactKey);

        // 닉네임 중복 확인
        if (userQueryRepository.findByNickname(request.getNickname())
            .isPresent()) {
            cacheRepository.deleteAll(keysToDelete);
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 연락처 중복 확인
        if (userQueryRepository.findByContact(contact)
            .isPresent()) {
            cacheRepository.deleteAll(keysToDelete);
            throw new CustomException(ErrorCode.USER_CONTACT_DUPLICATED);
        }
    }
}
