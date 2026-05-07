package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminGetTermsListTest {

    @Mock
    private TermsQueryRepository termsQueryRepository;

    @InjectMocks
    private AdminGetTermsList adminGetTermsList;

    @Test
    @DisplayName("count가 0이면 빈 PaginatedResponseDto 반환")
    void getTermsList_returnsEmpty_whenCountIsZero() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(null, null, 1, 10);
        when(termsQueryRepository.countByFilter(isNull(), isNull())).thenReturn(0L);

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(query);

        // then
        assertThat(result.data()).isEmpty();
        assertThat(result.page().totalCount()).isZero();
        verify(termsQueryRepository, times(1)).countByFilter(isNull(), isNull());
        verify(termsQueryRepository, never()).findByFilter(any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("type 필터로 조회시 filter 파라미터 전달 확인")
    void getTermsList_verifiesFilterParam_whenTypeFilterGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(TermsType.SERVICE, null, 1, 10);
        Terms terms = Terms.create(TermsType.SERVICE, "1.0", "서비스 이용약관", "https://notion.so/terms", true);
        when(termsQueryRepository.countByFilter(eq(TermsType.SERVICE), isNull())).thenReturn(1L);
        when(termsQueryRepository.findByFilter(eq(TermsType.SERVICE), isNull(), eq(1), eq(10)))
                .thenReturn(List.of(terms));

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(query);

        // then
        verify(termsQueryRepository, times(1)).findByFilter(eq(TermsType.SERVICE), isNull(), eq(1), eq(10));
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getType().value()).isEqualTo(TermsType.SERVICE);
    }

    @Test
    @DisplayName("type 필터에 유효한 TermsType 전달 시 countByFilter에 조건 포함 확인")
    void getTermsList_verifiesTypeCondition_whenValidTermsTypeGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(TermsType.PRIVACY, null, 1, 10);
        when(termsQueryRepository.countByFilter(eq(TermsType.PRIVACY), isNull())).thenReturn(0L);

        // when
        adminGetTermsList.execute(query);

        // then
        verify(termsQueryRepository).countByFilter(eq(TermsType.PRIVACY), isNull());
    }

    @Test
    @DisplayName("status 필터에 유효한 TermsStatus 전달 시 countByFilter에 조건 포함 확인")
    void getTermsList_verifiesStatusCondition_whenValidTermsStatusGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(null, TermsStatus.PUBLISHED, 1, 10);
        when(termsQueryRepository.countByFilter(isNull(), eq(TermsStatus.PUBLISHED))).thenReturn(0L);

        // when
        adminGetTermsList.execute(query);

        // then
        verify(termsQueryRepository).countByFilter(isNull(), eq(TermsStatus.PUBLISHED));
    }
}
