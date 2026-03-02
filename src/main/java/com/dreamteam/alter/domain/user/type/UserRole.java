package com.dreamteam.alter.domain.user.type;

import java.util.Map;

public enum UserRole {
    ROLE_USER,
    ROLE_MANAGER,
    ROLE_ADMIN
    ;

    public static Map<UserRole, String> describe() {
        return Map.of(
            UserRole.ROLE_USER, "일반 사용자",
            UserRole.ROLE_MANAGER, "매니저",
            UserRole.ROLE_ADMIN, "관리자"
        );
    }
}
