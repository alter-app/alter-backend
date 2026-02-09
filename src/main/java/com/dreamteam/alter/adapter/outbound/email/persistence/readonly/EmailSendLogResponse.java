package com.dreamteam.alter.adapter.outbound.email.persistence.readonly;

import com.dreamteam.alter.domain.email.type.EmailSendStatus;

import java.time.LocalDateTime;

public record EmailSendLogResponse(
        Long id,
        String email,
        String code,
        EmailSendStatus status,
        LocalDateTime createdAt
) {
}
