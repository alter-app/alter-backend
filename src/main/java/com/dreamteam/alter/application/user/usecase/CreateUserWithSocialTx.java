package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentRepository;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.entity.UserTermsAgreement;
import com.dreamteam.alter.domain.terms.port.outbound.UserTermsAgreementRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateUserWithSocialTx {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthLogRepository authLogRepository;
    private final NotificationConsentRepository notificationConsentRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    @Transactional
    public GenerateTokenResponseDto process(
        String contact,
        CreateUserWithSocialRequestDto request,
        SocialAuthInfo socialAuthInfo,
        boolean notificationConsent,
        boolean nightNotificationConsent,
        List<Terms> agreedTerms
    ) {
        // 사용자 생성
        User user = userRepository.save(User.createWithSocial(
            contact,
            request.getName(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday(),
            socialAuthInfo.getEmail()
        ));

        notificationConsentRepository.save(NotificationConsent.create(user, notificationConsent, nightNotificationConsent));

        List<UserTermsAgreement> agreements = agreedTerms.stream()
                .map(terms -> UserTermsAgreement.create(user, terms))
                .toList();
        userTermsAgreementRepository.saveAll(agreements);

        // 소셜 계정 연동
        UserSocial userSocial = UserSocial.create(
            user,
            socialAuthInfo.getProvider(),
            socialAuthInfo.getSocialId(),
            socialAuthInfo.getRefreshToken()
        );
        user.addUserSocial(userSocial);

        // Authorization, AuthLog 저장
        Authorization authorization = authService.generateAuthorization(user, TokenScope.APP);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }
}
