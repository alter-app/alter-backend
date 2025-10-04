package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCancelReputationRequestUseCase {
    void execute(ManagerActor actor, Long requestId);
}
