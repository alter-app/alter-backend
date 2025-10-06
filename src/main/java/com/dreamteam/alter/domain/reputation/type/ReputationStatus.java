package com.dreamteam.alter.domain.reputation.type;

import java.util.Map;

public enum ReputationStatus {
    REQUESTED,
    DECLINED,
    COMPLETED,
    DELETED,
    CANCELED
    ;

    public static Map<ReputationStatus, String> describe() {
        return Map.of(
            ReputationStatus.REQUESTED, "요청됨",
            ReputationStatus.DECLINED, "거절됨",
            ReputationStatus.COMPLETED, "작성 완료",
            ReputationStatus.DELETED, "삭제됨",
            ReputationStatus.CANCELED, "취소됨"
        );
    }

}
