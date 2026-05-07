package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;
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
    public PaginatedResponseDto<AdminTermsListItemResponseDto> execute(AdminGetTermsListQuery query) {
        long total = termsQueryRepository.countByFilter(query.type(), query.status());
        PageRequestDto pageRequest = PageRequestDto.of(query.page(), query.pageSize());

        if (total == 0) {
            return PaginatedResponseDto.empty(PageResponseDto.empty(pageRequest));
        }

        List<Terms> termsList = termsQueryRepository.findByFilter(
                query.type(), query.status(), query.page(), query.pageSize());

        PageResponseDto pageResponseDto = PageResponseDto.of(pageRequest, (int) total);
        List<AdminTermsListItemResponseDto> items = termsList.stream()
                .map(AdminTermsListItemResponseDto::from)
                .toList();

        return PaginatedResponseDto.of(pageResponseDto, items);
    }
}
