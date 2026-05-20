package com.dreamteam.alter.domain.notification.type;

import java.util.Map;

public enum NotificationType {
    GENERAL,
    SCHEDULE,
    SUBSTITUTE,
    REPUTATION,
    POSTING_APPLICATION,
    CHAT,
    WORKSPACE_INVITATION,
    JOIN_REQUEST,
    ;

    public static Map<NotificationType, String> describe() {
        return Map.of(
            NotificationType.GENERAL, "일반",
            NotificationType.SCHEDULE, "근무",
            NotificationType.SUBSTITUTE, "대타",
            NotificationType.REPUTATION, "평판",
            NotificationType.POSTING_APPLICATION, "알바 지원",
            NotificationType.CHAT, "채팅",
            NotificationType.WORKSPACE_INVITATION, "업장 초대",
            NotificationType.JOIN_REQUEST, "합류 요청"
        );
    }
}
