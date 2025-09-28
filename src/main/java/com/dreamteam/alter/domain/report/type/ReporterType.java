package com.dreamteam.alter.domain.report.type;

import java.util.Map;

public enum ReporterType {
    USER,
    MANAGER;

    public static Map<ReporterType, String> describe() {
        return Map.of(
            ReporterType.USER, "사용자",
            ReporterType.MANAGER, "매니저"
        );
    }
}
