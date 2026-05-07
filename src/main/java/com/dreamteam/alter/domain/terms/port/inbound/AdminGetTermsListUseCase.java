package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;

public interface AdminGetTermsListUseCase {

    PaginatedResponseDto<AdminTermsListItemResponseDto> execute(AdminGetTermsListQuery query);
}
