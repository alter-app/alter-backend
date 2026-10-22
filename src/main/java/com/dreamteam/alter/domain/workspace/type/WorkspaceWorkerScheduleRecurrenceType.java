package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum WorkspaceWorkerScheduleRecurrenceType {
    WEEKLY,
    MONTHLY;

    public static Map<WorkspaceWorkerScheduleRecurrenceType, String> describe() {
        return Map.of(WEEKLY, "매주", MONTHLY, "매월");
    }
}
