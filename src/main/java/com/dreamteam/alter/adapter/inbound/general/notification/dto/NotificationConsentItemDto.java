package com.dreamteam.alter.adapter.inbound.general.notification.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 수신 동의 항목 DTO")
public record NotificationConsentItemDto(
    @Schema(description = "동의 항목 타입")
    DescribedEnumDto<NotificationConsentType> type,

    @Schema(description = "동의 여부")
    boolean consent
) {
    public static NotificationConsentItemDto of(NotificationConsentType type, boolean consent) {
        return new NotificationConsentItemDto(
            DescribedEnumDto.of(type, NotificationConsentType.describe()),
            consent
        );
    }
}
