package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.inbound.UpdateNotificationConsentUseCase;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("updateNotificationConsent")
@RequiredArgsConstructor
@Transactional
public class UpdateNotificationConsent implements UpdateNotificationConsentUseCase {

    private final NotificationConsentRepository notificationConsentRepository;
    private final NotificationConsentQueryRepository notificationConsentQueryRepository;

    @Override
    public void execute(UpdateNotificationConsentCommand command) {
        NotificationConsent consent = notificationConsentQueryRepository.findByUser(command.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "알림 수신 설정을 찾을 수 없습니다."));

        consent.updateConsent(command.getType(), command.isConsent());
    }
}
