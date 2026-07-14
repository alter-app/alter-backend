package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.dreamteam.alter.domain.auth.type.TokenScope;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisChatMessageBroadcaster 테스트")
class RedisChatMessageBroadcasterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("broadcast 시 chat:broadcast 채널로 roomId·message를 담은 엔벌로프 JSON을 publish한다")
    void broadcast_엔벌로프_publish() throws Exception {
        // given
        RedisChatMessageBroadcaster sut = new RedisChatMessageBroadcaster(redisTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
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
}
