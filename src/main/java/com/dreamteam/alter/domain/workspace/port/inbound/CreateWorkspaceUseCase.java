package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateWorkspaceUseCase {
	void execute(AppActor actor, CreateWorkspaceRequestDto request);
}
