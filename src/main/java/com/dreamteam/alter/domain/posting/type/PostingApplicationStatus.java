package com.dreamteam.alter.domain.posting.type;

import java.util.Map;
import java.util.Set;

public enum PostingApplicationStatus {
    SUBMITTED,
    SHORTLISTED,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    EXPIRED,
    DELETED
    ;

    // 아직 살아있는 지원 (취소/불합격/만료/삭제 제외)
    public static final Set<PostingApplicationStatus> ACTIVE_STATUSES = Set.of(
        SUBMITTED,
        SHORTLISTED,
        ACCEPTED
    );

    public static Map<PostingApplicationStatus, String> describe() {
        return Map.of(
            PostingApplicationStatus.SUBMITTED, "지원 완료",
            PostingApplicationStatus.SHORTLISTED, "서류 합격",
            PostingApplicationStatus.ACCEPTED, "최종 합격",
            PostingApplicationStatus.REJECTED, "불합격",
            PostingApplicationStatus.CANCELLED, "지원 취소",
            PostingApplicationStatus.EXPIRED, "만료됨",
            PostingApplicationStatus.DELETED, "삭제됨"
        );
    }

}
