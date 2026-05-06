package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    void 필터_없이_전체_목록_조회_성공_빈_리스트_반환() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(null, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        Page<Terms> emptyPage = new PageImpl<>(Collections.emptyList());
        when(termsQueryRepository.findByFilter(any(TermsListFilterDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(filter, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        verify(termsQueryRepository, times(1)).findByFilter(any(TermsListFilterDto.class), any(Pageable.class));
    }

    @Test
    void type_필터로_조회시_filter_파라미터_전달_확인() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(TermsType.SERVICE, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        Page<Terms> page = new PageImpl<>(List.of(terms));
        when(termsQueryRepository.findByFilter(any(TermsListFilterDto.class), any(Pageable.class)))
                .thenReturn(page);

        // when
        PaginatedResponseDto<AdminTermsListItemResponseDto> result = adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository, times(1)).findByFilter(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getType()).isEqualTo(TermsType.SERVICE);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getType()).isEqualTo("SERVICE");
    }

    @Test
    void type_필터에_유효한_TermsType_전달_시_조건_포함_확인() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(TermsType.PRIVACY, null);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        Page<Terms> emptyPage = new PageImpl<>(Collections.emptyList());
        when(termsQueryRepository.findByFilter(any(TermsListFilterDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository).findByFilter(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getType()).isEqualTo(TermsType.PRIVACY);
        assertThat(filterCaptor.getValue().getStatus()).isNull();
    }

    @Test
    void status_필터에_유효한_TermsStatus_전달_시_조건_포함_확인() {
        // given
        TermsListFilterDto filter = new TermsListFilterDto(null, TermsStatus.PUBLISHED);
        PageRequestDto pageRequest = new PageRequestDto(1, 10);
        Page<Terms> emptyPage = new PageImpl<>(Collections.emptyList());
        when(termsQueryRepository.findByFilter(any(TermsListFilterDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        adminGetTermsList.execute(filter, pageRequest);

        // then
        ArgumentCaptor<TermsListFilterDto> filterCaptor = ArgumentCaptor.forClass(TermsListFilterDto.class);
        verify(termsQueryRepository).findByFilter(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(filterCaptor.getValue().getType()).isNull();
    }
}
