package com.dreamteam.alter.domain.terms.type;

import java.util.Map;

public enum TermsStatus {
    DRAFT,
    PUBLISHED,
    DEPRECATED,
    DELETED
    ;

    public static Map<TermsStatus, String> describe() {
        return Map.of(
            DRAFT, "작성 중",
            PUBLISHED, "게시됨",
            DEPRECATED, "폐기됨",
            DELETED, "삭제됨"
        );
    }
}
