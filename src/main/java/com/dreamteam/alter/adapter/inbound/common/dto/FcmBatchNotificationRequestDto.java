package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FCM 다중 알림 발송 요청 DTO")
public class FcmBatchNotificationRequestDto {

    @NotEmpty
    @Schema(description = "대상 사용자 ID 목록", example = "[1, 2, 3]")
    private List<Long> targetUserIds;

    @NotNull
    @Schema(description = "알림 범위", example = "APP")
    private TokenScope scope;

    @NotBlank
    @Schema(description = "알림 제목", example = "새로운 메시지")
    private String title;

    @NotBlank
    @Schema(description = "알림 내용", example = "새로운 메시지가 도착했습니다.")
    private String body;

    public static FcmBatchNotificationRequestDto of(List<Long> targetUserIds, TokenScope scope, String title, String body) {
        return new FcmBatchNotificationRequestDto(targetUserIds, scope, title, body);
    }
}
