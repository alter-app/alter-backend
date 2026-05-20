package com.dreamteam.alter.domain.notification.type;

import java.util.Map;

public enum NotificationConsentType {
    GENERAL,
    NIGHT,
    SUBSTITUTE,
    REPUTATION,
    ;

    public static Map<NotificationConsentType, String> describe() {
        return Map.of(
            NotificationConsentType.GENERAL, "전체 알림 수신",
            NotificationConsentType.NIGHT, "야간 알림 수신",
            NotificationConsentType.SUBSTITUTE, "대타 알림 수신",
            NotificationConsentType.REPUTATION, "평판 알림 수신"
        );
    }
}
