package com.dreamteam.alter.domain.notification.result;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;

import java.util.EnumMap;
import java.util.Map;

public record GetNotificationConsentResult(
    Map<NotificationConsentType, Boolean> consents
) {
    public static GetNotificationConsentResult from(NotificationConsent consent) {
        Map<NotificationConsentType, Boolean> consents = new EnumMap<>(NotificationConsentType.class);
        consents.put(NotificationConsentType.GENERAL, consent.isNotificationConsent());
        consents.put(NotificationConsentType.NIGHT, consent.isNightNotificationConsent());
        return new GetNotificationConsentResult(consents);
    }
}
