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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        PageRequest pageable = PageRequest.of(pageRequest.page() - 1, pageRequest.pageSize());
        Page<Terms> page = termsQueryRepository.findByFilter(filter, pageable);

        List<AdminTermsListItemResponseDto> data = page.getContent().stream()
                .map(AdminTermsListItemResponseDto::from)
                .toList();
        PageResponseDto pageResponse = PageResponseDto.of(pageRequest, (int) page.getTotalElements());

        return PaginatedResponseDto.of(pageResponse, data);
    }
}
