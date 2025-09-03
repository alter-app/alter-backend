package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialAuthInfo;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractSocialAuth {

    public SocialAuthInfo authenticate(SocialLoginRequestDto request) {
        SocialTokenResponseDto socialTokens = getToken(request);
        return getUserInfo(socialTokens);
    }

    protected SocialTokenResponseDto getToken(SocialLoginRequestDto request) {
        if (ObjectUtils.isNotEmpty(request.getAuthorizationCode()))
            return exchangeCodeForToken(request.getAuthorizationCode(), request.getPlatformType());

        if (ObjectUtils.isNotEmpty(request.getOauthToken()))
            return SocialTokenResponseDto.withAccessAndRefresh(
                request.getOauthToken().getAccessToken(),
                request.getOauthToken().getRefreshToken()
            );

        throw new IllegalArgumentException("Required fields are missing for provider: " + request.getProvider());
    }

    protected abstract SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType);

    protected abstract SocialAuthInfo getUserInfo(SocialTokenResponseDto socialTokens);

    public abstract boolean supports(SocialProvider provider);
}
