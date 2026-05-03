package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminUpdateTermsUseCase {

    void execute(Long id, AdminUpdateTermsRequestDto request, AdminActor actor);
}
