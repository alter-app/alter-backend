package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.apache.commons.lang3.ObjectUtils;

import java.util.regex.Pattern;

public class PhoneNumberUtil {

    public static final String LOCAL_PHONE_NUMBER_PATTERN = "(?:0[0-9]{8,10}|0[0-9]{1,2}-[0-9]{3,4}-[0-9]{4})";
    private static final Pattern LOCAL_PHONE_NUMBER = Pattern.compile(LOCAL_PHONE_NUMBER_PATTERN);
    private static final String KR_COUNTRY_CODE = "+82";
    private static final int E164_KR_LENGTH = 13; // +821012345678

    public static boolean isValidLocalNumber(String phoneNumber) {
        return phoneNumber != null && LOCAL_PHONE_NUMBER.matcher(phoneNumber).matches();
    }

    public static String normalizeLocalNumber(String phoneNumber) {
        if (!isValidLocalNumber(phoneNumber)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT,
                "연락처는 0으로 시작하는 9~11자리 숫자 또는 2~3자리-3~4자리-4자리 형식이어야 합니다.");
        }
        return phoneNumber.replace("-", "");
    }

    public static String convertE164ToLocal(String e164PhoneNumber) {
        if (ObjectUtils.isEmpty(e164PhoneNumber)
            || !e164PhoneNumber.startsWith(KR_COUNTRY_CODE)
            || e164PhoneNumber.length() != E164_KR_LENGTH) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "유효하지 않은 전화번호 형식입니다.");
        }

        return "0" + e164PhoneNumber.substring(KR_COUNTRY_CODE.length());
    }
}
