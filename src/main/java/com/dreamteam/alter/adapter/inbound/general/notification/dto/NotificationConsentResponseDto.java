package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "알림 수신 설정 조회 응답 DTO")
public class NotificationConsentResponseDto {

    @Schema(description = "알림 항목별 동의 상태")
    private List<NotificationConsentItemDto> items;

    public static NotificationConsentResponseDto from(GetNotificationConsentResult result) {
        List<NotificationConsentItemDto> items = Arrays.stream(NotificationConsentType.values())
            .map(type -> NotificationConsentItemDto.of(
                type,
                Boolean.TRUE.equals(result.consents().get(type))
            ))
            .toList();
        return NotificationConsentResponseDto.builder()
            .items(items)
            .build();
    }
}
