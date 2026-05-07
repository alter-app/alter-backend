package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("adminGetTermsList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetTermsList implements AdminGetTermsListUseCase {

    private final TermsQueryRepository termsQueryRepository;

    @Override
    public PaginatedResponseDto<AdminTermsListItemResponseDto> execute(TermsListFilterDto filter, PageRequestDto pageRequest) {
        long count = termsQueryRepository.countByFilter(filter.getType(), filter.getStatus());
        if (count == 0) {
            return PaginatedResponseDto.empty(PageResponseDto.empty(pageRequest));
        }

        List<Terms> termsList = termsQueryRepository.findByFilter(
                filter.getType(), filter.getStatus(), pageRequest.page(), pageRequest.pageSize());
        PageResponseDto pageResponse = PageResponseDto.of(pageRequest, (int) count);

        return PaginatedResponseDto.of(
            pageResponse,
            termsList.stream()
                .map(AdminTermsListItemResponseDto::from)
                .toList()
        );
    }
}
