package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;

public record FcmBatchNotificationEvent(FcmBatchNotificationRequestDto request) {
}
