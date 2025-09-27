package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateWorkerUseCase {
    void execute(ManagerActor actor, Long shiftId, Long newWorkerId);
}
