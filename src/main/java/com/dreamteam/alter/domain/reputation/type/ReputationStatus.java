package com.dreamteam.alter.domain.reputation.type;

import java.util.Map;
import java.util.Set;

public enum ReputationStatus {
    REQUESTED,
    DECLINED,
    COMPLETED,
    DELETED
    ;

    public static Map<ReputationStatus, String> describe() {
        return Map.of(
            ReputationStatus.REQUESTED, "요청됨",
            ReputationStatus.DECLINED, "거절됨",
            ReputationStatus.COMPLETED, "작성 완료",
            ReputationStatus.DELETED, "삭제됨"
        );
    }

    public static Set<ReputationStatus> unmodifiableStatus() {
        return Set.of(
            ReputationStatus.DECLINED,
            ReputationStatus.COMPLETED,
            ReputationStatus.DELETED
        );
    }

}
