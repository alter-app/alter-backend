package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum WorkspaceShiftStatus {
    PLANNED,
    CONFIRMED,
    CANCELLED,
    DELETED
    ;

    public static Map<WorkspaceShiftStatus, String> describe() {
        return Map.of(
            PLANNED, "미배정됨",
            CONFIRMED, "확정됨",
            CANCELLED, "취소됨",
            DELETED, "삭제됨"
        );
    }

}
