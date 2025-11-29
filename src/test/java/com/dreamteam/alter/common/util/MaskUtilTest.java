package com.dreamteam.alter.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("MaskUtil 테스트")
class MaskUtilTest {

    @Test
    @DisplayName("null, 빈 문자열, 공백만 있는 경우 원본 반환")
    void maskEmail_null_빈문자열_처리() {
        // given & when & then
        assertNull(MaskUtil.maskEmail(null));
        assertEquals("", MaskUtil.maskEmail(""));
        assertEquals("   ", MaskUtil.maskEmail("   "));
    }

    @Test
    @DisplayName("@ 기호가 없는 이메일 형식의 경우 원본 반환")
    void maskEmail_도메인이없는경우() {
        // given
        String email = "invalidemail";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("invalidemail", result);
    }

    @Test
    @DisplayName("일반적인 이메일 주소의 로컬 파트 절반 마스킹")
    void maskEmail_정상적인이메일마스킹() {
        // given
        String email = "testuser@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("test****@example.com", result);
    }

    @Test
    @DisplayName("로컬 파트가 1자인 경우 마스킹 동작 확인")
    void maskEmail_짧은로컬파트_1자() {
        // given
        String email = "a@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        // 1자일 경우 halfLength = 0, maskStartIndex = 1이므로 마스킹되지 않음
        assertEquals("a@example.com", result);
    }

    @Test
    @DisplayName("로컬 파트가 2자인 경우 마스킹 동작 확인")
    void maskEmail_짧은로컬파트_2자() {
        // given
        String email = "ab@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("a*@example.com", result);
    }

    @Test
    @DisplayName("로컬 파트가 긴 경우 마스킹 동작 확인")
    void maskEmail_긴로컬파트() {
        // given
        String email = "verylongemailaddress@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        // 20자일 경우 halfLength = 10, maskStartIndex = 10이므로 인덱스 10부터 마스킹
        assertEquals("verylongem**********@example.com", result);
    }

    @Test
    @DisplayName("도메인 부분은 마스킹되지 않는지 확인")
    void maskEmail_도메인은마스킹안됨() {
        // given
        String email = "testuser@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("test****@example.com", result);
        // 도메인 부분이 그대로 유지되는지 확인
        assert result.contains("@example.com");
    }

    @Test
    @DisplayName("로컬 파트가 3자인 경우 마스킹 동작 확인")
    void maskEmail_로컬파트_3자() {
        // given
        String email = "abc@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("ab*@example.com", result);
    }

    @Test
    @DisplayName("로컬 파트가 4자인 경우 마스킹 동작 확인")
    void maskEmail_로컬파트_4자() {
        // given
        String email = "abcd@example.com";

        // when
        String result = MaskUtil.maskEmail(email);

        // then
        assertEquals("ab**@example.com", result);
    }
}
