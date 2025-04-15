package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.fasterxml.jackson.databind.JsonNode;

public interface AppleAuthClient {
    SocialTokenResponseDto exchangeCodeForToken(String authorizationCode);
    JsonNode fetchPublicKeys();
}
