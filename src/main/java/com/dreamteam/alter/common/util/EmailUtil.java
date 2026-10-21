package com.dreamteam.alter.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class EmailUtil {

    private EmailUtil() {
    }

    /**
     * 이메일을 비교·저장용 형태(앞뒤 공백 제거 + 소문자)로 맞춥니다.
     */
    public static String normalize(String email) {
        return StringUtils.trim(StringUtils.lowerCase(email, Locale.ROOT));
    }
}
