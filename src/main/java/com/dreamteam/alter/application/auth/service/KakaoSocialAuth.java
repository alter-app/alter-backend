package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.auth.port.outbound.KakaoAuthClient;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.springframework.stereotype.Component;

@Component
public class KakaoSocialAuth extends AbstractSocialAuth {

    private final KakaoAuthClient kakaoAuthClient;

    public KakaoSocialAuth(KakaoAuthClient kakaoAuthClient) {
        this.kakaoAuthClient = kakaoAuthClient;
    }

    @Override
    protected SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType) {
        return kakaoAuthClient.exchangeCodeForToken(authorizationCode, platformType);
    }

    @Override
    protected SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        SocialUserInfo userInfo = kakaoAuthClient.getUserInfo(socialTokens);

        // refreshToken을 포함한 SocialUserInfo 반환
        return SocialUserInfo.of(
            userInfo.getProvider(),
            userInfo.getSocialId(),
            userInfo.getEmail(),
            socialTokens.getRefreshToken()
        );
    }

    @Override
    public boolean supports(SocialProvider provider) {
        return SocialProvider.KAKAO.equals(provider);
    }

}
