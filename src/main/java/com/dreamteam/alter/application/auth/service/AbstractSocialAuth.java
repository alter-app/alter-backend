package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.domain.auth.vo.SocialAuthRequest;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractSocialAuth {

    public SocialAuthInfo authenticate(SocialAuthRequest request) {
        SocialTokenResponseDto socialTokens = getToken(request);
        return getUserInfo(socialTokens);
    }

    protected SocialTokenResponseDto getToken(SocialAuthRequest request) {
        if (ObjectUtils.isNotEmpty(request.authorizationCode()))
            return exchangeCodeForToken(request.authorizationCode(), request.platformType());

        if (ObjectUtils.isNotEmpty(request.oauthToken()))
            return SocialTokenResponseDto.withAccessAndRefresh(
                request.oauthToken().getAccessToken(),
                request.oauthToken().getRefreshToken()
            );

        throw new IllegalArgumentException("Required fields are missing for provider: " + request.provider());
    }

    protected abstract SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType);

    protected abstract SocialAuthInfo getUserInfo(SocialTokenResponseDto socialTokens);

    public abstract boolean supports(SocialProvider provider);
}
