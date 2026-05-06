package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;

public interface AdminGetTermsDetailUseCase {

    AdminTermsDetailResponseDto execute(Long id);
}
