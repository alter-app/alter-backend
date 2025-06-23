package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.user.type.PlatformType;

public interface KakaoAuthClient {
    SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType);
    SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens);
}
