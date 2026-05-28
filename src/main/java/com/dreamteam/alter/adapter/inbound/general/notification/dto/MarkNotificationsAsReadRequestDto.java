package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 읽음 처리 요청 DTO")
public class MarkNotificationsAsReadRequestDto {

    @Schema(description = "읽음 처리할 알림 ID (null이면 전체 읽음 처리)", example = "1")
    private Long notificationId;
}
