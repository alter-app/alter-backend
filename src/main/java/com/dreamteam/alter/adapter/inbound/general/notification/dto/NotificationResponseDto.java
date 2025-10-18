package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import com.dreamteam.alter.adapter.outbound.notification.persistence.readonly.NotificationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "알림 조회 응답 DTO")
public class NotificationResponseDto {

    @Schema(description = "알림 ID")
    private Long id;

    @Schema(description = "알림 제목")
    private String title;

    @Schema(description = "알림 내용")
    private String body;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    public static NotificationResponseDto from(NotificationResponse response) {
        return NotificationResponseDto.builder()
            .id(response.id())
            .title(response.title())
            .body(response.body())
            .createdAt(response.createdAt())
            .build();
    }
}
