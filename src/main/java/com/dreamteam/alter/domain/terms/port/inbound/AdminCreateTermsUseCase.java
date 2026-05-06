package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;

public interface AdminCreateTermsUseCase {

    Long execute(AdminCreateTermsRequestDto request);
}
