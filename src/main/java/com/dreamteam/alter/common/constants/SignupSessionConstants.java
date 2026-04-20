package com.dreamteam.alter.common.constants;

public final class SignupSessionConstants {

    /**
     * 회원가입 세션 관련 상수
     */
    public static final class Session {
        public static final String KEY_PREFIX = "SIGNUP:PENDING:";
        public static final String CONTACT_INDEX_KEY_PREFIX = "SIGNUP:CONTACT:";
        public static final long EXPIRATION_MINUTES = 10; // 10분 후 만료
    }
}
