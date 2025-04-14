package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.auth.port.outbound.KakaoAuthClient;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoSocialAuth extends AbstractSocialAuth {

    private final KakaoAuthClient kakaoAuthClient;

    @Override
    protected SocialTokenResponseDto exchangeCodeForToken(String authorizationCode) {
        return kakaoAuthClient.exchangeCodeForToken(authorizationCode);
    }

    @Override
    protected SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        return kakaoAuthClient.getUserInfo(socialTokens);
    }

    @Override
    public boolean supports(SocialProvider provider) {
        return SocialProvider.KAKAO.equals(provider);
    }

}
