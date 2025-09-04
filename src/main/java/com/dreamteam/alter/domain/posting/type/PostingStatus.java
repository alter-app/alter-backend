package com.dreamteam.alter.domain.posting.type;

import java.util.Map;

public enum PostingStatus {
    OPEN,
    CLOSED,
    CANCELLED,
    DELETED,
    ;

    public static Map<PostingStatus, String> describe() {
        return Map.of(
            PostingStatus.OPEN, "모집 중",
            PostingStatus.CLOSED, "모집 완료",
            PostingStatus.CANCELLED, "취소됨",
            PostingStatus.DELETED, "삭제됨"
        );
    }
}
