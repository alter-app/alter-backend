package com.dreamteam.alter.domain.report.type;

import java.util.Map;
import java.util.Set;

public enum ReportStatus {
    PENDING,
    PROCESSING,
    RESOLVED,
    REJECTED,
    CANCELLED,
    DELETED;

    public static Map<ReportStatus, String> describe() {
        return Map.of(
            ReportStatus.PENDING, "대기중",
            ReportStatus.PROCESSING, "처리중",
            ReportStatus.RESOLVED, "완료",
            ReportStatus.REJECTED, "거부됨",
            ReportStatus.CANCELLED, "취소됨",
            ReportStatus.DELETED, "삭제됨"
        );
    }

    public static Set<ReportStatus> cancellableStatuses() {
        return Set.of(PENDING, PROCESSING);
    }
}
