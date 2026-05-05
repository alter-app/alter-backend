package com.dreamteam.alter.domain.notification.port.outbound;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;

public interface NotificationConsentRepository {
    NotificationConsent save(NotificationConsent consent);
}
