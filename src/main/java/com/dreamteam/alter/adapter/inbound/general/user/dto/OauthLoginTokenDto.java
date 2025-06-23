package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 토큰 DTO")
public class OauthLoginTokenDto {

    @NotBlank
    @Schema(description = "OAuth 액세스 토큰")
    private String accessToken;

    @NotBlank
    @Schema(description = "OAuth 리프레시 토큰")
    private String refreshToken;

}
