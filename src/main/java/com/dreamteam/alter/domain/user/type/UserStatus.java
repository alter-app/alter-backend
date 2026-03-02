package com.dreamteam.alter.domain.user.type;

import java.util.Map;

public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    DELETED
    ;

    public static Map<UserStatus, String> describe() {
        return Map.of(
            UserStatus.ACTIVE, "활성",
            UserStatus.SUSPENDED, "정지",
            UserStatus.DELETED, "삭제됨"
        );
    }
}
