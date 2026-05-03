package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetTermsListUseCase {

    PaginatedResponseDto<AdminTermsListItemResponseDto> execute(TermsListFilterDto filter, PageRequestDto pageRequest, AdminActor actor);
}
