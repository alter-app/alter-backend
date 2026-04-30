package com.dreamteam.alter.domain.user.port.inbound.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "소셜 계정 연동 상태")
public class SocialAccountStatusDto {

    @Schema(description = "소셜 플랫폼", example = "KAKAO")
    private SocialProvider provider;

    @Schema(description = "연동 여부", example = "true")
    private boolean linked;

    @Schema(description = "연동된 시점 (연동된 경우에만 제공)", nullable = true)
    private LocalDateTime linkedAt;

    public static SocialAccountStatusDto of(SocialProvider provider, boolean linked, LocalDateTime linkedAt) {
        return new SocialAccountStatusDto(provider, linked, linkedAt);
    }
}
