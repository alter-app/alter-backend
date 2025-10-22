package com.dreamteam.alter.adapter.outbound.notification.persistence.readonly;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String body,
    LocalDateTime createdAt
) {
}
