package com.dreamteam.alter.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 마스킹 유틸리티 클래스
 */
public class MaskUtil {

    private static final String MASK_CHAR = "*";

    /**
     * 이메일 주소를 마스킹 처리합니다.
     *
     * @param email 마스킹할 이메일 주소
     * @return 마스킹된 이메일 주소
     */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            // @ 기호가 없으면 전체를 마스킹
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        int localPartLength = localPart.length();
        int halfLength = localPartLength / 2;
        int maskStartIndex = localPartLength - halfLength;

        StringBuilder maskedLocalPart = new StringBuilder(localPart);
        for (int i = maskStartIndex; i < localPartLength; i++) {
            maskedLocalPart.setCharAt(i, MASK_CHAR.charAt(0));
        }

        return maskedLocalPart.toString() + domain;
    }
}
