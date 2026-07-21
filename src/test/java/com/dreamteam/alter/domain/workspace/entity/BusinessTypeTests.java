package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BusinessType 테스트")
class BusinessTypeTests {

    @Nested
    @DisplayName("resolveDetail")
    class ResolveDetailTests {

        @Test
        @DisplayName("'기타'가 아닌 업종이면 상세 입력이 들어와도 null 을 반환한다")
        void resolveDetail_비기타_null반환() {
            // given
            BusinessType businessType = BusinessTypeFixture.of(false);

            // when
            String detail = businessType.resolveDetail("무시되어야 함");

            // then
            assertThat(detail).isNull();
        }

        @Test
        @DisplayName("'기타' 업종인데 상세 입력이 공백뿐이면 ILLEGAL_ARGUMENT 예외가 발생한다")
        void resolveDetail_기타_상세없음_예외() {
            // given
            BusinessType businessType = BusinessTypeFixture.of(true);

            // when & then
            assertThatThrownBy(() -> businessType.resolveDetail("   "))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        }

        @Test
        @DisplayName("'기타' 업종이면 상세 입력을 trim 하여 반환한다")
        void resolveDetail_기타_상세있음_trim반환() {
            // given
            BusinessType businessType = BusinessTypeFixture.of(true);

            // when
            String detail = businessType.resolveDetail("  떡볶이 전문점  ");

            // then
            assertThat(detail).isEqualTo("떡볶이 전문점");
        }
    }
}
