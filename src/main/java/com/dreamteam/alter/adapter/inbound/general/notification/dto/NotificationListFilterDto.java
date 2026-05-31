package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import com.dreamteam.alter.domain.notification.type.NotificationType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "알림 목록 필터 DTO")
public class NotificationListFilterDto {

    @Parameter(description = "알림 유형 필터 (GENERAL | SCHEDULE | SUBSTITUTE | REPUTATION | POSTING_APPLICATION | CHAT | WORKSPACE_INVITATION | JOIN_REQUEST), 미입력 시 전체 조회")
    private NotificationType type;

    @Parameter(description = "읽음 여부 필터 (true: 읽은 알림만, false: 읽지 않은 알림만), 미입력 시 전체 조회")
    private Boolean isRead;
}
