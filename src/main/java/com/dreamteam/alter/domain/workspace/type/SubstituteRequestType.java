package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum SubstituteRequestType {
    ALL,
    SPECIFIC
    ;

    public static Map<SubstituteRequestType, String> describe() {
        return Map.of(
            ALL, "전체 대상",
            SPECIFIC, "특정 대상"
        );
    }

}
