package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum BusinessInvitationStatus {
    PENDING, ACCEPTED, DECLINED, EXPIRED
    ;

    public static Map<BusinessInvitationStatus, String> describe() {
        return Map.of(
            BusinessInvitationStatus.PENDING, "대기 중",
            BusinessInvitationStatus.ACCEPTED, "수락됨",
            BusinessInvitationStatus.DECLINED, "거절됨",
            BusinessInvitationStatus.EXPIRED, "만료됨"
        );
    }
}
