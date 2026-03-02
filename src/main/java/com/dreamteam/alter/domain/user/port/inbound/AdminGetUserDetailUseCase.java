package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserDetailResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetUserDetailUseCase {
    AdminUserDetailResponseDto execute(Long userId, AdminActor actor);
}
