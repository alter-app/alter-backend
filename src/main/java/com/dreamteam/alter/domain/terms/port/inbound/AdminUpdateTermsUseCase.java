package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;

public interface AdminUpdateTermsUseCase {

    void execute(Long id, AdminUpdateTermsRequestDto request);
}
