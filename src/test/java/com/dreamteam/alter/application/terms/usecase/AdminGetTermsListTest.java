package com.dreamteam.alter.application.terms.usecase;

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
    @DisplayName("필터 없이 전체 목록 조회 성공 빈 리스트 반환")
    void getTermsList_returnsEmptyList_whenNoFilter() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(null, null, 1, 10);
        when(termsQueryRepository.countByFilter(isNull(), isNull())).thenReturn(0L);
        when(termsQueryRepository.findByFilter(isNull(), isNull(), eq(1), eq(10))).thenReturn(List.of());

        // when
        long total = adminGetTermsList.count(query);
        List<Terms> result = adminGetTermsList.execute(query);

        // then
        assertThat(total).isZero();
        assertThat(result).isEmpty();
        verify(termsQueryRepository, times(1)).countByFilter(isNull(), isNull());
        verify(termsQueryRepository, times(1)).findByFilter(isNull(), isNull(), eq(1), eq(10));
    }

    @Test
    @DisplayName("type 필터로 조회시 filter 파라미터 전달 확인")
    void getTermsList_verifiesFilterParam_whenTypeFilterGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(TermsType.SERVICE, null, 1, 10);
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        when(termsQueryRepository.findByFilter(eq(TermsType.SERVICE), isNull(), eq(1), eq(10)))
                .thenReturn(List.of(terms));

        // when
        List<Terms> result = adminGetTermsList.execute(query);

        // then
        verify(termsQueryRepository, times(1)).findByFilter(eq(TermsType.SERVICE), isNull(), eq(1), eq(10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TermsType.SERVICE);
    }

    @Test
    @DisplayName("type 필터에 유효한 TermsType 전달 시 조건 포함 확인")
    void getTermsList_verifiesTypeCondition_whenValidTermsTypeGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(TermsType.PRIVACY, null, 1, 10);
        when(termsQueryRepository.countByFilter(eq(TermsType.PRIVACY), isNull())).thenReturn(0L);

        // when
        adminGetTermsList.count(query);

        // then
        verify(termsQueryRepository).countByFilter(eq(TermsType.PRIVACY), isNull());
    }

    @Test
    @DisplayName("status 필터에 유효한 TermsStatus 전달 시 조건 포함 확인")
    void getTermsList_verifiesStatusCondition_whenValidTermsStatusGiven() {
        // given
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(null, TermsStatus.PUBLISHED, 1, 10);
        when(termsQueryRepository.countByFilter(isNull(), eq(TermsStatus.PUBLISHED))).thenReturn(0L);

        // when
        adminGetTermsList.count(query);

        // then
        verify(termsQueryRepository).countByFilter(isNull(), eq(TermsStatus.PUBLISHED));
    }
}
