package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.fasterxml.jackson.databind.JsonNode;

public interface AppleAuthClient {
    SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType);
    JsonNode fetchPublicKeys();
}
