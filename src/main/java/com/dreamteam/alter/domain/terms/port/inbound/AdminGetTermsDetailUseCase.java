package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetTermsDetailUseCase {

    AdminTermsDetailResponseDto execute(Long id, AdminActor actor);
}
