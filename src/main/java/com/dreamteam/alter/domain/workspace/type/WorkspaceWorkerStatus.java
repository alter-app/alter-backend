package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum WorkspaceWorkerStatus {
    ACTIVATED,
    RESIGNED,
    ;

    public static Map<WorkspaceWorkerStatus, String> describe() {
        return Map.of(
            WorkspaceWorkerStatus.ACTIVATED, "재직 중",
            WorkspaceWorkerStatus.RESIGNED, "퇴사"
        );
    }
}
