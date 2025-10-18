package com.dreamteam.alter.domain.workspace.type;

import java.util.List;
import java.util.Map;

public enum SubstituteRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED_BY_TARGET,
    APPROVED,
    REJECTED_BY_APPROVER,
    CANCELLED,
    EXPIRED
    ;

    public static Map<SubstituteRequestStatus, String> describe() {
        return Map.of(
            PENDING, "대기 중",
            ACCEPTED, "수락됨",
            REJECTED_BY_TARGET, "거절됨",
            APPROVED, "승인됨",
            REJECTED_BY_APPROVER, "관리자 거절",
            CANCELLED, "취소됨",
            EXPIRED, "만료됨"
        );
    }

    /**
     * 매니저가 조회할 수 있는 대타 요청 상태 목록을 반환합니다.
     * 매니저는 수락된 요청(승인 대기), 승인된 요청, 거절된 요청을 조회합니다.
     * @return 매니저 조회 가능 상태 목록
     */
    public static List<SubstituteRequestStatus> getManagerViewableStatuses() {
        return List.of(
            ACCEPTED,
            APPROVED,
            REJECTED_BY_APPROVER
        );
    }

    /**
     * 해당 상태가 매니저가 조회할 수 있는 상태인지 확인합니다.
     * @param status 확인할 상태
     * @return 매니저 조회 가능 여부
     */
    public static boolean isManagerViewable(SubstituteRequestStatus status) {
        return getManagerViewableStatuses().contains(status);
    }

}
