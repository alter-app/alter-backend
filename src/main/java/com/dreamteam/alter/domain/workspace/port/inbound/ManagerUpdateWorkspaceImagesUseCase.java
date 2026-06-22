package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceImagesRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdateWorkspaceImagesUseCase {
    void execute(ManagerActor actor, Long workspaceId, UpdateWorkspaceImagesRequestDto request);
}
