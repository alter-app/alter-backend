package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageBroadcaster implements ChatMessageBroadcaster {

    public static final String CHANNEL = "chat:broadcast";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void broadcast(Long roomId, ChatMessageResponse message) {
        try {
            String payload = objectMapper.writeValueAsString(new ChatBroadcastEnvelope(roomId, message));
            redisTemplate.convertAndSend(CHANNEL, payload);
        } catch (Exception e) {
            log.error("채팅 메시지 Redis 발행 실패. RoomId: {}, Error: {}", roomId, e.getMessage(), e);
        }
    }
}
