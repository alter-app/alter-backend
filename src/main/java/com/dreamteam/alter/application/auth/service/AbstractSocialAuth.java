package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginUserRequestDto;
import com.dreamteam.alter.domain.auth.port.outbound.SocialRefreshTokenRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractSocialAuth {

    protected final SocialRefreshTokenRepository socialRefreshTokenRepository;

    protected AbstractSocialAuth(SocialRefreshTokenRepository socialRefreshTokenRepository) {
        this.socialRefreshTokenRepository = socialRefreshTokenRepository;
    }

    public SocialUserInfo authenticate(LoginUserRequestDto request) {
        SocialTokenResponseDto socialTokens = getToken(request);
        return getUserInfo(socialTokens);
    }

    protected SocialTokenResponseDto getToken(LoginUserRequestDto request) {
        if (ObjectUtils.isNotEmpty(request.getAuthorizationCode()))
            return exchangeCodeForToken(request.getAuthorizationCode());

        if (ObjectUtils.isNotEmpty(request.getAccessToken()))
            return new SocialTokenResponseDto(request.getAccessToken());

        throw new IllegalArgumentException("Required fields are missing for provider: " + request.getProvider());
    }

    protected abstract SocialTokenResponseDto exchangeCodeForToken(String authorizationCode);

    protected abstract SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens);

    public abstract boolean supports(SocialProvider provider);

    protected void saveOrUpdateRefreshToken(SocialProvider provider, String socialId, String refreshToken) {
        socialRefreshTokenRepository.saveOrUpdate(provider, socialId, refreshToken);
    }

}
