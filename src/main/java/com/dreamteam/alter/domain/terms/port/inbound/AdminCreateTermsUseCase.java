package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.성terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminCreateTermsUseCase {

    Long execute(AdminCreateTermsRequestDto request, AdminActor actor);
}
