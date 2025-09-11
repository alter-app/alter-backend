package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 로그인 요청 DTO")
public class SocialLoginRequestDto {

    @NotNull
    @Schema(description = "소셜 플랫폼 플랫폼", example = "KAKAO")
    private SocialProvider provider;

    @Schema(description = "OAuth 토큰")
    private OauthLoginTokenDto oauthToken;

    @Schema(description = "OAuth 인가 코드", example = "authorizationCode")
    private String authorizationCode;

    @NotNull
    @Schema(description = "플랫폼 타입", example = "WEB / NATIVE")
    private PlatformType platformType;
}
