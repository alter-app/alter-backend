package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.inbound.GetNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getNotificationConsent")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetNotificationConsent implements GetNotificationConsentUseCase {

    private final NotificationConsentQueryRepository notificationConsentQueryRepository;

    @Override
    public GetNotificationConsentResult execute(GetNotificationConsentCommand command) {
        NotificationConsent consent = notificationConsentQueryRepository.findByUser(command.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "알림 수신 설정을 찾을 수 없습니다."));

        return new GetNotificationConsentResult(consent.isNotificationConsent(), consent.isNightNotificationConsent());
    }
}
