package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface WorkspaceDeclineReputationRequestUseCase {
    void execute(ManagerActor actor, Long requestId);
}
