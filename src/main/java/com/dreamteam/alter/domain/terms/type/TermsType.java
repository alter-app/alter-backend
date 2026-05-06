package com.dreamteam.alter.domain.terms.type;

import java.util.Map;

public enum TermsType {
    SERVICE,
    PRIVACY,
    LOCATION,
    MARKETING
    ;

    public static Map<TermsType, String> describe() {
        return Map.of(
            SERVICE, "서비스 이용약관",
            PRIVACY, "개인정보 처리방침",
            LOCATION, "위치정보 수집 및 이용",
            MARKETING, "마케팅 정보 수신"
        );
    }
}
