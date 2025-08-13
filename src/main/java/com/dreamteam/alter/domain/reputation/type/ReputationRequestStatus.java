package com.dreamteam.alter.domain.reputation.type;

import java.util.Map;

public enum ReputationRequestStatus {
    REQUESTED,
    EXPIRED,
    DECLINED,
    COMPLETED
    ;

    public static Map<ReputationRequestStatus, String> describe() {
        return Map.of(
            ReputationRequestStatus.REQUESTED, "요청됨",
            ReputationRequestStatus.EXPIRED, "만료됨",
            ReputationRequestStatus.DECLINED, "거절됨",
            ReputationRequestStatus.COMPLETED, "작성 완료"
        );
    }

}
