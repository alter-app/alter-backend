package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerApproveSubstituteRequestUseCase {
    void execute(ManagerActor actor, Long requestId, ApproveSubstituteRequestDto request);
}
