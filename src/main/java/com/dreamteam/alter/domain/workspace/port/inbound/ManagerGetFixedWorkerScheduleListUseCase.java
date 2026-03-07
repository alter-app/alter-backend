package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface ManagerGetFixedWorkerScheduleListUseCase {
	List<WorkspaceWorkerSchedule> execute(ManagerActor actor, Long workspaceId);
}
