package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetUserListUseCase {
    PaginatedResponseDto<AdminUserListResponseDto> execute(
        PageRequestDto request,
        AdminUserListFilterDto filter,
        AdminActor actor
    );
}
