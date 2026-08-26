package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatSessionRevokeEnvelope;
import com.dreamteam.alter.common.config.ChatWebSocketSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 멤버십 강제종료(session-revoke) 신호를 받아, 이 인스턴스가 물고 있는 그 유저의
 * WebSocketSession을 모두 close한다. 특정 방 구독만 골라 해제할 수 없어 세션 전체를
 * 닫는다 — 클라이언트는 재연결 후 구독을 다시 등록하며, 이미 끊긴 방의 SUBSCRIBE는
 * {@code ChatSubscribeAuthorizationChannelInterceptor}가 조용히 드롭한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionRevokeRedisSubscriber implements MessageListener {

    private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatSessionRevokeEnvelope envelope = objectMapper.readValue(body, ChatSessionRevokeEnvelope.class);
            List<WebSocketSession> sessions =
                chatWebSocketSessionRegistry.findLocalSessions(envelope.getScope(), envelope.getMemberId());
            for (WebSocketSession session : sessions) {
                closeSession(session, envelope);
            }
        } catch (Exception e) {
            log.error("채팅 세션 강제종료 Redis 수신 처리 실패. Error: {}", e.getMessage(), e);
        }
    }

    private void closeSession(WebSocketSession session, ChatSessionRevokeEnvelope envelope) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("chat membership revoked"));
        } catch (Exception e) {
            // 세션 하나가 close에 실패해도 나머지 세션 종료는 계속한다.
            log.error("채팅 세션 강제종료 실패. sessionId={}, memberId={}, scope={}, roomId={}, Error: {}",
                session.getId(), envelope.getMemberId(), envelope.getScope(), envelope.getRoomId(), e.getMessage(), e);
        }
    }
}
