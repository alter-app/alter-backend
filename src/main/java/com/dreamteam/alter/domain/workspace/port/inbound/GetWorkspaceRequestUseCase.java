package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestResponseDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface GetWorkspaceRequestUseCase {
	WorkspaceRequestResponseDto execute(User user, Long workspaceRequestId);
}
