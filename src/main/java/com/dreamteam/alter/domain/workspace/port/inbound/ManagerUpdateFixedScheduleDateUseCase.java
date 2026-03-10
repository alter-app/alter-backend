package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateFixedScheduleDateRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateFixedScheduleDateUseCase {
	void execute(ManagerActor actor, Long workspaceId, UpdateFixedScheduleDateRequestDto request);
}
