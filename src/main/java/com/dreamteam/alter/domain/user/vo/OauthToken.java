package com.dreamteam.alter.domain.user.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthToken {

    private final String accessToken;
    private final String refreshToken;

    public static OauthToken of(String accessToken, String refreshToken) {
        return new OauthToken(accessToken, refreshToken);
    }
}
