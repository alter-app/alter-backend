package com.dreamteam.alter.domain.notification.result;

public record GetNotificationConsentResult(
    boolean notificationConsent,
    boolean nightNotificationConsent
) {
}
