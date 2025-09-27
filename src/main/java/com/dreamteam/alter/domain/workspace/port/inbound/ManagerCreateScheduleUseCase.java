package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.CreateWorkScheduleRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCreateScheduleUseCase {
    void execute(ManagerActor actor, CreateWorkScheduleRequestDto request);
}
