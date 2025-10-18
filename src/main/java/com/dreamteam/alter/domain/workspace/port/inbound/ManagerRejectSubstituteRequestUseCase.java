package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerRejectSubstituteRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerRejectSubstituteRequestUseCase {
    void execute(ManagerActor actor, Long requestId, ManagerRejectSubstituteRequestDto request);
}
