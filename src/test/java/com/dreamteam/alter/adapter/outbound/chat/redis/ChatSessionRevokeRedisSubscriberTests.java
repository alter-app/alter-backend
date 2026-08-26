package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatSessionRevokeEnvelope;
import com.dreamteam.alter.common.config.ChatWebSocketSessionRegistry;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSessionRevokeRedisSubscriber 테스트")
class ChatSessionRevokeRedisSubscriberTests {

    @Mock
    private ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatSessionRevokeRedisSubscriber sut(ChatWebSocketSessionRegistry registry) {
        return new ChatSessionRevokeRedisSubscriber(registry, objectMapper);
    }

    private ChatSessionRevokeRedisSubscriber sut() {
        return sut(chatWebSocketSessionRegistry);
    }

    private DefaultMessage redisMessage(TokenScope scope, Long memberId, Long roomId) throws Exception {
        String payload = objectMapper.writeValueAsString(new ChatSessionRevokeEnvelope(scope, memberId, roomId));
        return new DefaultMessage(RedisChatSessionRevocationBroadcaster.CHANNEL.getBytes(StandardCharsets.UTF_8),
            payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("로컬에 있는 해당 유저 세션만 close한다")
    void onMessage_해당유저_세션만_close() throws Exception {
        // given
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        given(chatWebSocketSessionRegistry.findLocalSessions(TokenScope.APP, 1L))
            .willReturn(List.of(session1, session2));

        // when
        sut().onMessage(redisMessage(TokenScope.APP, 1L, 100L), null);

        // then
        then(session1).should().close(any(CloseStatus.class));
        then(session2).should().close(any(CloseStatus.class));
    }

    @Test
    @DisplayName("유저 A·B 세션을 모두 등록해도 A에 대한 revoke는 A 세션만 close하고 B는 건드리지 않는다")
    void onMessage_다른유저는_close안함() throws Exception {
        // given: 실제 레지스트리에 유저 A(APP,1L), 유저 B(APP,2L) 세션을 각각 등록
        ChatWebSocketSessionRegistry registry = new ChatWebSocketSessionRegistry();

        WebSocketSession sessionA = mock(WebSocketSession.class);
        when(sessionA.getId()).thenReturn("sess-a");
        registry.registerSession(sessionA);
        registry.linkMember(TokenScope.APP, 1L, "sess-a");

        WebSocketSession sessionB = mock(WebSocketSession.class);
        when(sessionB.getId()).thenReturn("sess-b");
        registry.registerSession(sessionB);
        registry.linkMember(TokenScope.APP, 2L, "sess-b");

        // when: 유저 A에 대한 revoke만 수신
        sut(registry).onMessage(redisMessage(TokenScope.APP, 1L, 100L), null);

        // then: A 세션만 close되고 B 세션은 close되지 않는다
        then(sessionA).should().close(any(CloseStatus.class));
        then(sessionB).should(never()).close(any(CloseStatus.class));
    }

    @Test
    @DisplayName("세션 하나가 close에서 예외를 던져도 나머지 세션은 닫힌다")
    void onMessage_일부close_실패해도_나머지는_닫힌다() throws Exception {
        // given
        WebSocketSession failing = mock(WebSocketSession.class);
        WebSocketSession ok = mock(WebSocketSession.class);
        willThrow(new RuntimeException("close failed")).given(failing).close(any(CloseStatus.class));
        given(chatWebSocketSessionRegistry.findLocalSessions(TokenScope.APP, 1L)).willReturn(List.of(failing, ok));

        // when
        sut().onMessage(redisMessage(TokenScope.APP, 1L, 100L), null);

        // then
        then(ok).should().close(any(CloseStatus.class));
    }

    @Test
    @DisplayName("역직렬화 불가능한 메시지는 예외 없이 skip한다")
    void onMessage_잘못된_JSON_skip() {
        // given
        DefaultMessage message = new DefaultMessage(
            RedisChatSessionRevocationBroadcaster.CHANNEL.getBytes(StandardCharsets.UTF_8),
            "not-a-json".getBytes(StandardCharsets.UTF_8));

        // when
        sut().onMessage(message, null);

        // then
        then(chatWebSocketSessionRegistry).should(never()).findLocalSessions(any(), any());
    }
}
