package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateWorkerScheduleUseCase {
	void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId, UpdateWorkerScheduleRequestDto request);
}
