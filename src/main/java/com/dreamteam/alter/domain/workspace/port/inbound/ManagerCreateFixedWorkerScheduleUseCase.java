package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.CreateFixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCreateFixedWorkerScheduleUseCase {
	void execute(ManagerActor actor, Long workspaceId, CreateFixedWorkerScheduleCommand command);
}
