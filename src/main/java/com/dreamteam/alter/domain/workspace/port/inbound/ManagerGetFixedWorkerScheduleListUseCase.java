package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.result.FixedWorkerScheduleResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetFixedWorkerScheduleListUseCase {
	List<FixedWorkerScheduleResult> execute(ManagerActor actor, Long workspaceId);
}
