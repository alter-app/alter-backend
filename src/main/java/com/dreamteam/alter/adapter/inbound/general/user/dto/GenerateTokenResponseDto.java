package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "토큰 발급 응답 DTO")
public class GenerateTokenResponseDto {

    @Schema(description = "인증 ID", example = "UUID")
    private String authorizationId;

    @Schema(description = "토큰 scope", example = "APP")
    private TokenScope scope;

    @Schema(description = "엑세스 토큰", example = "accessToken")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "refreshToken")
    private String refreshToken;

    public static GenerateTokenResponseDto of(Authorization authorization) {
        return GenerateTokenResponseDto.builder()
            .authorizationId(authorization.getId())
            .scope(authorization.getScope())
            .accessToken(authorization.getAccessToken())
            .refreshToken(authorization.getRefreshToken())
            .build();
    }

}
