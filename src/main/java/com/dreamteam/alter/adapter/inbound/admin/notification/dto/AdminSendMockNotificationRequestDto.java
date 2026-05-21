package com.dreamteam.alter.adapter.inbound.admin.notification.dto;

import com.dreamteam.alter.domain.notification.type.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 모의 알림 전송 요청 DTO")
public class AdminSendMockNotificationRequestDto {

    @NotNull
    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "알림 제목")
    private String title;

    @Schema(description = "알림 내용")
    private String body;

    @Schema(description = "알림 타입 (null 시 GENERAL)")
    private NotificationType type;

}
