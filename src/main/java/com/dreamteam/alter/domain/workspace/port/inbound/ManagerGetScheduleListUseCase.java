package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.GetManagerScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetScheduleListUseCase {
    GetManagerScheduleResponseDto execute(ManagerActor actor, Long workspaceId, int year, int month);
}
