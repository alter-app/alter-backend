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
}
