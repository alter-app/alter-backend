package com.dreamteam.alter.domain.auth.vo;

import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.vo.OauthToken;

public record SocialAuthRequest(
    SocialProvider provider,
    OauthToken oauthToken,
    String authorizationCode,
    PlatformType platformType
) {
}
