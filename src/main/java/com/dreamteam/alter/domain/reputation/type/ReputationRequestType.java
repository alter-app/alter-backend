package com.dreamteam.alter.domain.reputation.type;

import java.util.Set;

public enum ReputationRequestType {
    USER_TO_USER_INTERNAL,
    USER_TO_USER_EXTERNAL,
    USER_TO_WORKSPACE,
    WORKSPACE_TO_USER
    ;

    public static Set<ReputationRequestType> userAvailableTypes() {
        return Set.of(
            USER_TO_USER_INTERNAL,
            USER_TO_USER_EXTERNAL,
            USER_TO_WORKSPACE
        );
    }

}
