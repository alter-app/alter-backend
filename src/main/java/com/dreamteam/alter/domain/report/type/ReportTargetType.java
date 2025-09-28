package com.dreamteam.alter.domain.report.type;

import java.util.Map;

public enum ReportTargetType {
    USER,
    REPUTATION,
    POSTING,
    WORKSPACE;

    public static Map<ReportTargetType, String> describe() {
        return Map.of(
            ReportTargetType.USER, "사용자",
            ReportTargetType.REPUTATION, "평판",
            ReportTargetType.POSTING, "공고",
            ReportTargetType.WORKSPACE, "업장"
        );
    }
}
