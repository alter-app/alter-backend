package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
}
