package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisChatMessageBroadcaster 테스트")
class RedisChatMessageBroadcasterTests {

    @Mock
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("broadcast 시 chat:broadcast 채널로 roomId·message를 담은 엔벌로프 JSON을 publish한다")
    void broadcast_엔벌로프_publish() throws Exception {
        // given
        RedisChatMessageBroadcaster sut = new RedisChatMessageBroadcaster(redisTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
        );

        // when
        sut.broadcast(100L, message);

        // then
        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        then(redisTemplate).should().convertAndSend(channelCaptor.capture(), payloadCaptor.capture());

        assertThat(channelCaptor.getValue()).isEqualTo("chat:broadcast");
        ChatBroadcastEnvelope decoded = objectMapper.readValue(payloadCaptor.getValue(), ChatBroadcastEnvelope.class);
        assertThat(decoded.getRoomId()).isEqualTo(100L);
        assertThat(decoded.getMessage().getId()).isEqualTo(10L);
        assertThat(decoded.getMessage().getContent()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("Redis publish 중 RuntimeException이 발생해도 broadcast는 예외를 전파하지 않는다")
    void broadcast_publish_실패시_예외를_전파하지_않는다() {
        // given
        RedisChatMessageBroadcaster sut = new RedisChatMessageBroadcaster(redisTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
        );
        willThrow(new RuntimeException("redis connection failed")).given(redisTemplate).convertAndSend(any(), any());

        // when & then
        assertThatCode(() -> sut.broadcast(100L, message)).doesNotThrowAnyException();
    }
}
