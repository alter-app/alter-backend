package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PhoneNumberUtil 테스트")
class PhoneNumberUtilTests {

    @Test
    @DisplayName("유효한 한국 E.164 번호를 로컬 형식으로 변환")
    void convertE164ToLocal_유효한번호_변환성공() {
        // given & when
        String result = PhoneNumberUtil.convertE164ToLocal("+821012345678");

        // then
        assertEquals("01012345678", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열은 ILLEGAL_ARGUMENT 예외 발생")
    void convertE164ToLocal_빈값_예외발생(String input) {
        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            PhoneNumberUtil.convertE164ToLocal(input);
        });

        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+15551234567", "+441234567890"})
    @DisplayName("한국 번호가 아닌 경우 ILLEGAL_ARGUMENT 예외 발생")
    void convertE164ToLocal_비한국번호_예외발생(String input) {
        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            PhoneNumberUtil.convertE164ToLocal(input);
        });

        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+8210123456", "+82101234567890"})
    @DisplayName("길이가 맞지 않는 한국 번호는 ILLEGAL_ARGUMENT 예외 발생")
    void convertE164ToLocal_잘못된길이_예외발생(String input) {
        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            PhoneNumberUtil.convertE164ToLocal(input);
        });

        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, exception.getErrorCode());
    }
}
