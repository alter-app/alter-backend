package com.dreamteam.alter.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 비밀번호 검증 유틸리티 클래스
 */
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 16;
    private static final String LETTER_PATTERN = ".*[a-zA-Z].*";
    private static final String DIGIT_PATTERN = ".*[0-9].*";
    private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>?/~].*";

    /**
     * 비밀번호 형식이 규칙에 맞는지 검증합니다.
     *
     * @param password 검증할 비밀번호
     * @return 검증 통과 여부
     */
    public static boolean isValid(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }

        // 길이 검증 (8~16자)
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }

        // 영문 포함 여부
        if (!password.matches(LETTER_PATTERN)) {
            return false;
        }

        // 숫자 포함 여부
        if (!password.matches(DIGIT_PATTERN)) {
            return false;
        }

        // 특수문자 포함 여부
        if (!password.matches(SPECIAL_CHAR_PATTERN)) {
            return false;
        }

        return true;
    }
}
