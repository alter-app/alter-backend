package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserPasswordRequestDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminUpdateUserPasswordUseCase {
    void execute(Long userId, AdminUpdateUserPasswordRequestDto request, AdminActor actor);
}
