package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerTodayScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

import java.util.List;

public interface ManagerGetDailyScheduleListUseCase {
    List<ManagerTodayScheduleResponseDto> execute(ManagerActor actor, Long workspaceId);
}
