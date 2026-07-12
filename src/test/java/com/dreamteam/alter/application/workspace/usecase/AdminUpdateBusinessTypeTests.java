package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.AdminUpdateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("AdminUpdateBusinessType 테스트")
class AdminUpdateBusinessTypeTests {

    @Mock
    private BusinessTypeRepository businessTypeRepository;

    @InjectMocks
    private AdminUpdateBusinessType adminUpdateBusinessType;

    @Test
    @DisplayName("존재하지 않는 업종이면 NOT_FOUND 예외가 발생한다")
    void execute_미존재_예외() {
        // given
        given(businessTypeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUpdateBusinessType.execute(1L, new AdminUpdateBusinessTypeCommand("카페", null)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    @DisplayName("자신을 제외한 다른 업종과 이름이 중복되면 CONFLICT 예외가 발생한다")
    void execute_이름중복_예외() {
        // given
        BusinessType businessType = mock(BusinessType.class);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(businessTypeRepository.existsByNameAndIdNot("카페", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminUpdateBusinessType.execute(1L, new AdminUpdateBusinessTypeCommand("카페", null)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        then(businessType).should(never()).update(any(), any());
    }

    @Test
    @DisplayName("중복이 없으면 이름/설명을 수정한다")
    void execute_성공() {
        // given
        BusinessType businessType = mock(BusinessType.class);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(businessTypeRepository.existsByNameAndIdNot("카페", 1L)).willReturn(false);

        // when
        adminUpdateBusinessType.execute(1L, new AdminUpdateBusinessTypeCommand("카페", "카페/디저트"));

        // then
        then(businessType).should().update("카페", "카페/디저트");
    }
}
