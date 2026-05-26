package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "미읽음 알림 개수 조회 응답 DTO")
public class UnreadNotificationCountResponseDto {

    @Schema(description = "미읽음 알림 개수", example = "3")
    private long unreadCount;

    @Schema(description = "미읽음 알림 존재 여부", example = "true")
    private boolean hasUnread;

    public static UnreadNotificationCountResponseDto of(long unreadCount) {
        return UnreadNotificationCountResponseDto.builder()
            .unreadCount(unreadCount)
            .hasUnread(unreadCount > 0)
            .build();
    }
}
