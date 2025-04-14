package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;

public interface KakaoAuthClient {
    SocialTokenResponseDto exchangeCodeForToken(String authorizationCode);
    SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens);
}
