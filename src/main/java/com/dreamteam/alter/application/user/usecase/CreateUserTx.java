package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
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
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateUserTx {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuthLogRepository authLogRepository;
    private final NotificationConsentRepository notificationConsentRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    @Transactional
    public GenerateTokenResponseDto process(
        CreateUserRequestDto request,
        String contact,
        String verifiedEmail,
        boolean notificationConsent,
        boolean nightNotificationConsent,
        List<Terms> agreedTerms
    ) {
        // 사용자 생성
        User user = userRepository.save(User.create(
            contact,
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday(),
            verifiedEmail
        ));

        notificationConsentRepository.save(NotificationConsent.create(user, notificationConsent, nightNotificationConsent));

        List<UserTermsAgreement> agreements = agreedTerms.stream()
                .map(terms -> UserTermsAgreement.create(user, terms))
                .toList();
        userTermsAgreementRepository.saveAll(agreements);

        Authorization authorization = authService.generateAuthorization(user, TokenScope.APP);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }
}
