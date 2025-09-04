package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum WorkspaceStatus {
    PENDING,
    ACTIVATED,
    CLOSED,
    REVOKED,
    DELETED
    ;

    public static Map<WorkspaceStatus, String> describe() {
        return Map.of(
            WorkspaceStatus.PENDING, "승인 대기",
            WorkspaceStatus.ACTIVATED, "활성화",
            WorkspaceStatus.CLOSED, "폐업",
            WorkspaceStatus.REVOKED, "승인 취소",
            WorkspaceStatus.DELETED, "삭제됨"
        );
    }
}
