package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.apache.commons.lang3.ObjectUtils;

public class PhoneNumberUtil {

    private static final String KR_COUNTRY_CODE = "+82";
    private static final int E164_KR_LENGTH = 13; // +821012345678

    public static String convertE164ToLocal(String e164PhoneNumber) {
        if (ObjectUtils.isEmpty(e164PhoneNumber)
            || !e164PhoneNumber.startsWith(KR_COUNTRY_CODE)
            || e164PhoneNumber.length() != E164_KR_LENGTH) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "유효하지 않은 전화번호 형식입니다.");
        }

        return "0" + e164PhoneNumber.substring(KR_COUNTRY_CODE.length());
    }
}
