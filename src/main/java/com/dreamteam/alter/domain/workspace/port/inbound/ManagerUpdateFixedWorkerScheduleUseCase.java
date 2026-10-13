package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateFixedWorkerScheduleUseCase {
	void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId, FixedWorkerScheduleCommand command);
}
