package com.dreamteam.alter.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFcmNotification(FcmNotificationEvent event) {
        try {
            notificationService.sendNotification(event.request());
        } catch (Exception e) {
            log.warn("FCM 알림 발송 실패. targetUserId={}, error={}",
                event.request().getTargetUserId(), e.getMessage());
        }
    }
}
