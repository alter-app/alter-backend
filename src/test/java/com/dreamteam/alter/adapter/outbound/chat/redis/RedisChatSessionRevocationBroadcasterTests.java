package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatSessionRevokeEnvelope;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisChatSessionRevocationBroadcaster 테스트")
class RedisChatSessionRevocationBroadcasterTests {

    @Mock
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("revoke 시 chat:session-revoke 채널로 scope·memberId·roomId를 담은 엔벌로프 JSON을 publish한다")
    void revoke_엔벌로프_publish() throws Exception {
        // given
        RedisChatSessionRevocationBroadcaster sut = new RedisChatSessionRevocationBroadcaster(redisTemplate, objectMapper);

        // when
        sut.revoke(TokenScope.APP, 1L, 100L);

        // then
        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        then(redisTemplate).should().convertAndSend(channelCaptor.capture(), payloadCaptor.capture());

        assertThat(channelCaptor.getValue()).isEqualTo("chat:session-revoke");
        ChatSessionRevokeEnvelope decoded = objectMapper.readValue(payloadCaptor.getValue(), ChatSessionRevokeEnvelope.class);
        assertThat(decoded.getScope()).isEqualTo(TokenScope.APP);
        assertThat(decoded.getMemberId()).isEqualTo(1L);
        assertThat(decoded.getRoomId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Redis publish 중 RuntimeException이 발생해도 revoke는 예외를 전파하지 않는다")
    void revoke_publish_실패시_예외를_전파하지_않는다() {
        // given
        RedisChatSessionRevocationBroadcaster sut = new RedisChatSessionRevocationBroadcaster(redisTemplate, objectMapper);
        willThrow(new RuntimeException("redis connection failed")).given(redisTemplate).convertAndSend(any(), any());

        // when & then
        assertThatCode(() -> sut.revoke(TokenScope.APP, 1L, 100L)).doesNotThrowAnyException();
    }
}
