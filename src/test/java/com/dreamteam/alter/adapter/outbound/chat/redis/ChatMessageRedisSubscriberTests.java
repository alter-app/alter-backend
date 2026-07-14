package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageRedisSubscriber 테스트")
class ChatMessageRedisSubscriberTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("엔벌로프 메시지를 수신하면 /sub/chat.{roomId}로 로컬 전송한다")
    void onMessage_정상_로컬전송() throws Exception {
        // given
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
        );
        String payload = objectMapper.writeValueAsString(new ChatBroadcastEnvelope(100L, message));
        DefaultMessage redisMessage = new DefaultMessage("chat:broadcast".getBytes(StandardCharsets.UTF_8),
            payload.getBytes(StandardCharsets.UTF_8));

        // when
        sut.onMessage(redisMessage, null);

        // then
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.100"), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("역직렬화 불가능한 메시지는 예외 없이 skip하며 로컬 전송하지 않는다")
    void onMessage_잘못된_JSON_skip() {
        // given
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        DefaultMessage redisMessage = new DefaultMessage("chat:broadcast".getBytes(StandardCharsets.UTF_8),
            "not-a-json".getBytes(StandardCharsets.UTF_8));

        // when
        sut.onMessage(redisMessage, null);

        // then
        then(messagingTemplate).should(never()).convertAndSend(any(String.class), any(Object.class));
    }
}
