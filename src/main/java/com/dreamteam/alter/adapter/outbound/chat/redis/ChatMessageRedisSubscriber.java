package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatBroadcastEnvelope envelope = objectMapper.readValue(body, ChatBroadcastEnvelope.class);
            messagingTemplate.convertAndSend("/sub/chat." + envelope.getRoomId(), envelope.getMessage());
        } catch (Exception e) {
            log.error("채팅 메시지 Redis 수신 처리 실패. Error: {}", e.getMessage(), e);
        }
    }
}
