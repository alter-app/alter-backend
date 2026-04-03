package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;

public interface GetAdminWorkspaceRequestUseCase {

	AdminWorkspaceRequestResponseDto execute(Long workspaceRequestId);
}
