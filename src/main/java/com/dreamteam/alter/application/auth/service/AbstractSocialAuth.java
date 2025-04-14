package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserLoginRequestDto;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractSocialAuth {

    public SocialUserInfo authenticate(UserLoginRequestDto request) {
        SocialTokenResponseDto socialTokens = getToken(request);
        return getUserInfo(socialTokens);
    }

    protected SocialTokenResponseDto getToken(UserLoginRequestDto request) {
        if (ObjectUtils.isNotEmpty(request.getAuthorizationCode()))
            return exchangeCodeForToken(request.getAuthorizationCode());

        if (ObjectUtils.isNotEmpty(request.getAccessToken()))
            return new SocialTokenResponseDto(request.getAccessToken());

        throw new IllegalArgumentException("Required fields are missing for provider: " + request.getProvider());
    }

    protected abstract SocialTokenResponseDto exchangeCodeForToken(String authorizationCode);

    protected abstract SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens);

    public abstract boolean supports(SocialProvider provider);

}
