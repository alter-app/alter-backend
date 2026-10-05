package com.dreamteam.alter.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FcmNotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFcmNotification(FcmNotificationEvent event) {
        notificationService.sendNotificationAfterCommit(event.request());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFcmBatchNotification(FcmBatchNotificationEvent event) {
        notificationService.sendMultipleNotificationsAfterCommit(event.request());
    }
}
