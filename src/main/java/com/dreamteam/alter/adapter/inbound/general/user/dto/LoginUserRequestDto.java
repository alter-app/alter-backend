package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "소셜 로그인 요청 DTO")
public class LoginUserRequestDto {

    @NotNull
    @Schema(description = "소셜 로그인 플랫폼", example = "KAKAO")
    private SocialProvider provider;

    @Valid
    @Schema(description = "OAuth 토큰")
    private OauthLoginTokenDto oauthToken;

    @Schema(description = "OAuth 인가 코드", example = "authorizationCode")
    private String authorizationCode;

    @NotNull
    @Schema(description = "플랫폼 타입", example = "WEB / NATIVE")
    private PlatformType platformType;

}
