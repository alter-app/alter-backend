package com.dreamteam.alter.domain.workspace.type;

import java.util.List;
import java.util.Map;

public enum SubstituteRequestTargetStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    ACCEPTED_BY_OTHERS,
    APPROVED,
    CANCELLED,
    REJECTED_BY_APPROVER,
    EXPIRED
    ;

    public static Map<SubstituteRequestTargetStatus, String> describe() {
        return Map.of(
            PENDING, "대기 중",
            ACCEPTED, "수락됨",
            REJECTED, "거절됨",
            ACCEPTED_BY_OTHERS, "다른 사람에게 수락됨",
            APPROVED, "승인됨",
            CANCELLED, "취소됨",
            REJECTED_BY_APPROVER, "관리자 거절",
            EXPIRED, "만료됨"
        );
    }

    /**
     * 응답 가능한 상태 목록을 반환합니다.
     * @return 응답 가능 상태 목록
     */
    public static List<SubstituteRequestTargetStatus> getRespondableStatuses() {
        return List.of(PENDING);
    }

    /**
     * 더 이상 응답할 수 없는 상태 목록을 반환합니다.
     * @return 응답 불가능 상태 목록
     */
    public static List<SubstituteRequestTargetStatus> getNonRespondableStatuses() {
        return List.of(ACCEPTED, REJECTED, ACCEPTED_BY_OTHERS, APPROVED, CANCELLED, REJECTED_BY_APPROVER, EXPIRED);
    }

    /**
     * 해당 상태가 응답 가능한 상태인지 확인합니다.
     * @param status 확인할 상태
     * @return 응답 가능 여부
     */
    public static boolean isRespondable(SubstituteRequestTargetStatus status) {
        return getRespondableStatuses().contains(status);
    }
}
