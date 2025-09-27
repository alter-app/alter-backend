package com.dreamteam.alter.domain.workspace.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum WorkerPositionType {
    OWNER("점주", "👑"),
    MANAGER("매니저", "👨‍💼"),
    WORKER("알바생", "👷");

    private final String description;
    private final String emoji;

    public static Map<WorkerPositionType, String> describe() {
        return Map.of(
            OWNER, OWNER.getDescription(),
            MANAGER, MANAGER.getDescription(),
            WORKER, WORKER.getDescription()
        );
    }
}
