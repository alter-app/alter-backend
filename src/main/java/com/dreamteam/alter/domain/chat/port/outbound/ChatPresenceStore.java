package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

import java.util.Collection;
import java.util.Set;

public interface ChatPresenceStore {

    void markOnline(TokenScope scope, Long memberId, String sessionId);

    void markOffline(TokenScope scope, Long memberId, String sessionId);

    // 인바운드 STOMP 활동(SUBSCRIBE/SEND/heartbeat) 시 presence TTL을 갱신한다.
    void touch(TokenScope scope, Long memberId);

    boolean isOnline(TokenScope scope, Long memberId);

    // 여러 대상의 온라인 여부를 한 번에 조회한다(멤버별 왕복 대신 파이프라인 1회).
    Set<PresenceTarget> filterOnline(Collection<PresenceTarget> targets);

    record PresenceTarget(TokenScope scope, Long memberId) {
    }
}
