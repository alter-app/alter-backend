package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.command.UpdateWorkspaceWorkerColorCommand;

public interface ManagerUpdateWorkspaceWorkerColorCodeUseCase {
    void execute(ManagerActor actor, Long workspaceId, Long workerId, UpdateWorkspaceWorkerColorCommand command);
}
