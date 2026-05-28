package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerResignWorkspaceWorkerUseCase {
    void execute(ManagerActor actor, Long workspaceId, Long workerId);
}
