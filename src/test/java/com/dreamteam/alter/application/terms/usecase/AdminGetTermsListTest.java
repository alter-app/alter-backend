package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        TermsListFilterDto filter = new TermsListFilterDto(null, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        when(termsQueryRepository.countByFilter(any(TermsListFilterDto.class))).thenReturn(0L);

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(filter, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        verify(termsQueryRepository, times(1)).countByFilter(any(TermsListFilterDto.class));
        verify(termsQueryRepository, never()).findByFilter(any(TermsListFilterDto.class), any(PageRequestDto.class));
    }

    @Test
    @DisplayName("type 필터로 조회시 filter 파라미터 전달 확인")
    void getTermsList_verifiesFilterParam_whenTypeFilterGiven() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(TermsType.SERVICE, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        when(termsQueryRepository.countByFilter(any(TermsListFilterDto.class))).thenReturn(1L);
        when(termsQueryRepository.findByFilter(any(TermsListFilterDto.class), any(PageRequestDto.class)))
                .thenReturn(List.of(terms));

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository, times(1)).findByFilter(filterCaptor.capture(), any(PageRequestDto.class));
        assertThat(filterCaptor.getValue().getType()).isEqualTo(TermsType.SERVICE);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getType().value()).isEqualTo(TermsType.SERVICE);
    }

    @Test
    @DisplayName("type 필터에 유효한 TermsType 전달 시 조건 포함 확인")
    void getTermsList_verifiesTypeCondition_whenValidTermsTypeGiven() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(TermsType.PRIVACY, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        when(termsQueryRepository.countByFilter(any(TermsListFilterDto.class))).thenReturn(0L);

        // when
        adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository).countByFilter(filterCaptor.capture());
        assertThat(filterCaptor.getValue().getType()).isEqualTo(TermsType.PRIVACY);
        assertThat(filterCaptor.getValue().getStatus()).isNull();
    }

    @Test
    @DisplayName("status 필터에 유효한 TermsStatus 전달 시 조건 포함 확인")
    void getTermsList_verifiesStatusCondition_whenValidTermsStatusGiven() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(null, TermsStatus.PUBLISHED);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        when(termsQueryRepository.countByFilter(any(TermsListFilterDto.class))).thenReturn(0L);

        // when
        adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository).countByFilter(filterCaptor.capture());
        assertThat(filterCaptor.getValue().getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(filterCaptor.getValue().getType()).isNull();
    }
}
