package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PhoneNumberUtil 테스트")
class PhoneNumberUtilTests {

    @ParameterizedTest
    @CsvSource({"010-1234-5678,01012345678", "01012345678,01012345678", "02-123-4567,021234567",
        "021234567,021234567", "02-1234-5678,0212345678", "0212345678,0212345678",
        "031-123-4567,0311234567", "0311234567,0311234567", "070-1234-5678,07012345678",
        "07012345678,07012345678", "099-1234-5678,09912345678", "09912345678,09912345678"})
    void 국내형_번호를_검증하고_하이픈만_제거한다(String input, String expected) {
        assertThat(PhoneNumberUtil.isValidLocalNumber(input)).isTrue();
        assertThat(PhoneNumberUtil.normalizeLocalNumber(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", " 01012345678", "01012345678 ", "01012a45678", "+821012345678",
        "010-12345678", "0101234-5678", "010--1234-5678", "010.1234.5678", "010 1234 5678",
        "010–1234–5678", "０１０１２３４５６７８", "123456789", "01234567", "012345678901",
        "010-12345-6789", "0-1234-5678", "01012345678\n"})
    void 잘못된_국내형_번호는_정규화하지_않는다(String input) {
        assertThat(PhoneNumberUtil.isValidLocalNumber(input)).isFalse();
        CustomException exception = assertThrows(CustomException.class, () -> PhoneNumberUtil.normalizeLocalNumber(input));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(exception.getMessage()).isEqualTo("연락처는 0으로 시작하는 9~11자리 숫자 또는 2~3자리-3~4자리-4자리 형식이어야 합니다.");
    }

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
