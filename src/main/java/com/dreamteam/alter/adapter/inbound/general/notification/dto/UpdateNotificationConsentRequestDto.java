package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 수신 설정 변경 요청 DTO")
public class UpdateNotificationConsentRequestDto {

    @NotNull
    @Schema(description = "알림 수신 동의 여부")
    private Boolean notificationConsent;

    @NotNull
    @Schema(description = "야간 알림 수신 동의 여부")
    private Boolean nightNotificationConsent;
}
