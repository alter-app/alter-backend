package com.dreamteam.alter.domain.notification.result;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;

import java.util.Map;

public record GetNotificationConsentResult(
    Map<NotificationConsentType, Boolean> consents
) {
    public static GetNotificationConsentResult from(NotificationConsent consent) {
        return new GetNotificationConsentResult(Map.of(
            NotificationConsentType.GENERAL, consent.isNotificationConsent(),
            NotificationConsentType.NIGHT, consent.isNightNotificationConsent()
        ));
    }
}
