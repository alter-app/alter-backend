package com.dreamteam.alter.common.config;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이 인스턴스가 물고 있는 WebSocketSession을 추적해, 멤버십이 끊긴 유저의 세션을
 * 강제 종료할 때 조회 대상으로 쓴다. 두 개의 맵으로 나뉘어 있다:
 * <p>
 * - {@code sessionId -> WebSocketSession}: raw WebSocket 핸드셰이크 시점(연결/종료)에
 *   {@code WebSocketConfig}의 {@code WebSocketHandlerDecoratorFactory}가 채운다. 이 시점엔
 *   아직 STOMP CONNECT 인증 전이라 유저를 알 수 없다 — sessionId만 안다.
 * - {@code (scope, memberId) -> sessionId 집합}: STOMP CONNECT/DISCONNECT 시점에
 *   {@code WebSocketEventListener}가 채운다(이미 presence markOnline/markOffline과 같은
 *   자리에서 principal과 sessionId를 둘 다 들고 있다).
 * <p>
 * 두 맵 모두 이 인스턴스 로컬 상태만 담으므로, 다른 인스턴스가 물고 있는 세션은 자연히
 * 조회되지 않는다(멀티 인스턴스 전파는 Redis pub/sub으로 별도 처리).
 */
@Component
public class ChatWebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessionsById = new ConcurrentHashMap<>();
    private final Map<MemberKey, Set<String>> sessionIdsByMember = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        sessionsById.put(session.getId(), session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessionsById.remove(session.getId());
    }

    public void linkMember(TokenScope scope, Long memberId, String sessionId) {
        sessionIdsByMember.computeIfAbsent(new MemberKey(scope, memberId), k -> ConcurrentHashMap.newKeySet())
            .add(sessionId);
    }

    public void unlinkMember(TokenScope scope, Long memberId, String sessionId) {
        sessionIdsByMember.computeIfPresent(new MemberKey(scope, memberId), (key, sessionIds) -> {
            sessionIds.remove(sessionId);
            return sessionIds.isEmpty() ? null : sessionIds;
        });
    }

    public List<WebSocketSession> findLocalSessions(TokenScope scope, Long memberId) {
        Set<String> sessionIds = sessionIdsByMember.get(new MemberKey(scope, memberId));
        if (sessionIds == null) {
            return List.of();
        }
        return sessionIds.stream()
            .map(sessionsById::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private record MemberKey(TokenScope scope, Long memberId) {
    }
}
