package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;

public interface GetAdminWorkspaceRequestListUseCase {

	CursorPaginatedApiResponse<AdminWorkspaceRequestListResponseDto> execute(CursorPageRequestDto request);
}
