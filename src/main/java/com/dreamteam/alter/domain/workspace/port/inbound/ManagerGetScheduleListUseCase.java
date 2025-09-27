package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerScheduleResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

import java.util.List;

public interface ManagerGetScheduleListUseCase {
    List<ManagerScheduleResponseDto> execute(ManagerActor actor, Long workspaceId, int year, int month);
}
