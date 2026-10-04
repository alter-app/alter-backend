package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum InvitationUnavailableReason {
    NOT_REGISTERED, ACCOUNT_UNAVAILABLE, ALREADY_WORKING, ALREADY_INVITED
    ;

    public static Map<InvitationUnavailableReason, String> describe() {
        return Map.of(
            InvitationUnavailableReason.NOT_REGISTERED, "가입되지 않은 번호입니다.",
            InvitationUnavailableReason.ACCOUNT_UNAVAILABLE, "이용할 수 없는 계정입니다.",
            InvitationUnavailableReason.ALREADY_WORKING, "이미 근무 중인 사용자입니다.",
            InvitationUnavailableReason.ALREADY_INVITED, "이미 초대 대기 중인 사용자입니다."
        );
    }
}
