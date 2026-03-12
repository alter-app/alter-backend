package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;

public record FcmNotificationEvent(FcmNotificationRequestDto request) {
}
