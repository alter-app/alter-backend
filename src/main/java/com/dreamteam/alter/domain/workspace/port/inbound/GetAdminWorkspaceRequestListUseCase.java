package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;

public interface GetAdminWorkspaceRequestListUseCase {

	PaginatedResponseDto<AdminWorkspaceRequestListResponseDto> execute(PageRequestDto request);
}
