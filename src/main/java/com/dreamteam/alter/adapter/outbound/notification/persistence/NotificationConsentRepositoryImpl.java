package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationConsentRepositoryImpl implements NotificationConsentRepository {

    private final NotificationConsentJpaRepository notificationConsentJpaRepository;

    @Override
    public NotificationConsent save(NotificationConsent consent) {
        return notificationConsentJpaRepository.save(consent);
    }
}
