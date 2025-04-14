package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialTokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private String identityToken;

    public SocialTokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public SocialTokenResponseDto(String refreshToken, String identityToken) {
        this.refreshToken = refreshToken;
        this.identityToken = identityToken;
    }

}
