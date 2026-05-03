package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "알림 수신 설정 조회 응답 DTO")
public class NotificationConsentResponseDto {

    @Schema(description = "알림 수신 동의 여부")
    private boolean notificationConsent;

    @Schema(description = "야간 알림 수신 동의 여부")
    private boolean nightNotificationConsent;

    public static NotificationConsentResponseDto from(GetNotificationConsentResult result) {
        return NotificationConsentResponseDto.builder()
            .notificationConsent(result.notificationConsent())
            .nightNotificationConsent(result.nightNotificationConsent())
            .build();
    }
}
