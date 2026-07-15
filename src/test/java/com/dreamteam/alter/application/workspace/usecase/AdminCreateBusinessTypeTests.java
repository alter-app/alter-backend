package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.AdminCreateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCreateBusinessType 테스트")
class AdminCreateBusinessTypeTests {

    @Mock
    private BusinessTypeRepository businessTypeRepository;

    @InjectMocks
    private AdminCreateBusinessType adminCreateBusinessType;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("이름이 중복되면 CONFLICT 예외가 발생하고 저장하지 않는다")
        void execute_이름중복_예외() {
            // given
            given(businessTypeRepository.existsByName("카페")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminCreateBusinessType.execute(new AdminCreateBusinessTypeCommand("카페", null)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
            then(businessTypeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("중복이 없으면 업종을 저장하고 id 를 반환한다")
        void execute_성공() {
            // given
            given(businessTypeRepository.existsByName("고기집")).willReturn(false);
            BusinessType saved = mock(BusinessType.class);
            given(saved.getId()).willReturn(7L);
            given(businessTypeRepository.save(any(BusinessType.class))).willReturn(saved);

            // when
            Long id = adminCreateBusinessType.execute(new AdminCreateBusinessTypeCommand("고기집", "고기 전문점"));

            // then
            assertThat(id).isEqualTo(7L);
            ArgumentCaptor<BusinessType> captor = ArgumentCaptor.forClass(BusinessType.class);
            then(businessTypeRepository).should().save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("고기집");
            assertThat(captor.getValue().isRequiresDetail()).isFalse();
        }
    }
}
