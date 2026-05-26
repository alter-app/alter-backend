package com.dreamteam.alter.adapter.outbound.notification.persistence.readonly;

import com.dreamteam.alter.domain.notification.type.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String body,
    boolean isRead,
    LocalDateTime createdAt
) {
}
