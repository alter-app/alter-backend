package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatSessionRevokeEnvelope;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatSessionRevocationBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatSessionRevocationBroadcaster implements ChatSessionRevocationBroadcaster {

    public static final String CHANNEL = "chat:session-revoke";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void revoke(TokenScope scope, Long memberId, Long roomId) {
        try {
            String payload = objectMapper.writeValueAsString(new ChatSessionRevokeEnvelope(scope, memberId, roomId));
            redisTemplate.convertAndSend(CHANNEL, payload);
        } catch (Exception e) {
            log.error("채팅 세션 강제종료 Redis 발행 실패. memberId={}, scope={}, roomId={}, Error: {}",
                memberId, scope, roomId, e.getMessage(), e);
        }
    }
}
