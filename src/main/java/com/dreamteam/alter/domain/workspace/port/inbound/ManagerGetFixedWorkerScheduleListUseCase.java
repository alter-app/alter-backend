package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.FixedWorkerScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetFixedWorkerScheduleListUseCase {
	List<FixedWorkerScheduleResponseDto> execute(ManagerActor actor, Long workspaceId);
}
