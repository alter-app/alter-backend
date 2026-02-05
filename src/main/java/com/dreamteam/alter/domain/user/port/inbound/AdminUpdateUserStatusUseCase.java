package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminUpdateUserStatusUseCase {
    void execute(Long userId, AdminUpdateUserStatusRequestDto request, AdminActor actor);
}
