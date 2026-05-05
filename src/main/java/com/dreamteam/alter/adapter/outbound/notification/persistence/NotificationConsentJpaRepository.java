package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationConsentJpaRepository extends JpaRepository<NotificationConsent, Long> {
}
