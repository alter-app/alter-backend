package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDeleteBusinessType 테스트")
class AdminDeleteBusinessTypeTests {

    @Mock
    private BusinessTypeRepository businessTypeRepository;

    @Mock
    private BusinessTypeQueryRepository businessTypeQueryRepository;

    @InjectMocks
    private AdminDeleteBusinessType adminDeleteBusinessType;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("존재하지 않는 업종이면 NOT_FOUND 예외가 발생한다")
        void execute_미존재_예외() {
            // given
            given(businessTypeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminDeleteBusinessType.execute(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
            then(businessTypeRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("'기타'(requiresDetail) 업종은 삭제할 수 없어 CONFLICT 예외가 발생한다")
        void execute_기타_예외() {
            // given
            BusinessType etc = mock(BusinessType.class);
            given(etc.isRequiresDetail()).willReturn(true);
            given(businessTypeRepository.findById(1L)).willReturn(Optional.of(etc));

            // when & then
            assertThatThrownBy(() -> adminDeleteBusinessType.execute(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
            then(businessTypeRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("업장 또는 업장 신청에서 사용 중이면 CONFLICT 예외가 발생한다")
        void execute_사용중_예외() {
            // given
            BusinessType businessType = mock(BusinessType.class);
            given(businessType.isRequiresDetail()).willReturn(false);
            given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
            given(businessTypeQueryRepository.existsReferenced(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminDeleteBusinessType.execute(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
            then(businessTypeRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("사용 중이 아닌 일반 업종은 삭제된다")
        void execute_성공() {
            // given
            BusinessType businessType = mock(BusinessType.class);
            given(businessType.isRequiresDetail()).willReturn(false);
            given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
            given(businessTypeQueryRepository.existsReferenced(1L)).willReturn(false);

            // when
            adminDeleteBusinessType.execute(1L);

            // then
            then(businessTypeRepository).should().delete(businessType);
        }
    }
}
