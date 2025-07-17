package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetWorkspaceUseCase {
    ManagerWorkspaceResponseDto execute(ManagerActor actor, Long workspaceId);
}
