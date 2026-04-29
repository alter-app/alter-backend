package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface CreateWorkspaceRequestUseCase {
	void execute(User user, CreateWorkspaceRequestDto request);
}
