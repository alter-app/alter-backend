package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialTokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private String identityToken;

    public static SocialTokenResponseDto withAccessAndRefresh(String accessToken, String refreshToken) {
        return new SocialTokenResponseDto(accessToken, refreshToken, null);
    }

    public static SocialTokenResponseDto withRefreshAndIdentity(String refreshToken, String identityToken) {
        return new SocialTokenResponseDto(null, refreshToken, identityToken);
    }

}
