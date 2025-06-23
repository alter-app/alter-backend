package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.auth.port.outbound.KakaoAuthClient;
import com.dreamteam.alter.domain.auth.port.outbound.SocialRefreshTokenRepository;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.springframework.stereotype.Component;

@Component
public class KakaoSocialAuth extends AbstractSocialAuth {

    private final KakaoAuthClient kakaoAuthClient;

    public KakaoSocialAuth(
        SocialRefreshTokenRepository socialRefreshTokenRepository,
        KakaoAuthClient kakaoAuthClient
    ) {
        super(socialRefreshTokenRepository);
        this.kakaoAuthClient = kakaoAuthClient;
    }

    @Override
    protected SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType) {
        return kakaoAuthClient.exchangeCodeForToken(authorizationCode, platformType);
    }

    @Override
    protected SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        SocialUserInfo userInfo = kakaoAuthClient.getUserInfo(socialTokens);

        // RefreshToken 저장
        saveOrUpdateRefreshToken(SocialProvider.KAKAO, userInfo.getSocialId(), socialTokens.getRefreshToken());

        return userInfo;
    }

    @Override
    public boolean supports(SocialProvider provider) {
        return SocialProvider.KAKAO.equals(provider);
    }

}
