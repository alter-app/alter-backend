package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.WorkspaceCreateReputationRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface WorkspaceCreateReputationUseCase {
    void execute(ManagerActor actor, WorkspaceCreateReputationRequestDto request);
}
