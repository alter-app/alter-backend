package com.dreamteam.alter.domain.auth.type;

import java.util.Map;

public enum TokenScope {
    APP,
    MANAGER,
    ADMIN;

    public static Map<TokenScope, String> describe() {
        return Map.of(
            TokenScope.APP, "일반 사용자",
            TokenScope.MANAGER, "매니저 사용자",
            TokenScope.ADMIN, "관리자"
        );
    }

    /**
     * STOMP 유저 목적지({@code convertAndSendToUser})의 principal 이름을 만든다.
     * {@code AccessTokenAuthentication.getName()}과 채팅 수신자 이름 계산이 반드시
     * 이 메서드를 공유해야 한다 — 각자 조립하면 한쪽만 바뀔 때 배달이 조용히 끊긴다.
     */
    public String principalName(Long id) {
        return this.name() + ":" + id;
    }
}
