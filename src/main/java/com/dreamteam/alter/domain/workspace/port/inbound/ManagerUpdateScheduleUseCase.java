package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateScheduleUseCase {
    void execute(ManagerActor actor, Long shiftId, UpdateWorkScheduleRequestDto request);
}
