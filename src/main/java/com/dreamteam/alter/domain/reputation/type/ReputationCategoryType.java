package com.dreamteam.alter.domain.reputation.type;

import java.util.Map;

public enum ReputationCategoryType {
    ATTITUDE,
    ENVIRONMENT,
    COMPENSATION,
    TEAMWORK,
    DIFFICULTY,
    ETC
    ;

    public static Map<ReputationCategoryType, String> describe() {
        return Map.of(
            ReputationCategoryType.ATTITUDE, "태도",
            ReputationCategoryType.ENVIRONMENT, "근무 환경",
            ReputationCategoryType.COMPENSATION, "복지",
            ReputationCategoryType.TEAMWORK, "팀워크",
            ReputationCategoryType.DIFFICULTY, "업무 난이도",
            ReputationCategoryType.ETC, "기타"
        );
    }

}
