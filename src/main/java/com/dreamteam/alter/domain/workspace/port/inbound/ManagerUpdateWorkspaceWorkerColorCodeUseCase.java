package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceWorkerColorRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateWorkspaceWorkerColorCodeUseCase {
    void execute(ManagerActor actor, Long workspaceId, Long workerId, UpdateWorkspaceWorkerColorRequestDto request);
}
