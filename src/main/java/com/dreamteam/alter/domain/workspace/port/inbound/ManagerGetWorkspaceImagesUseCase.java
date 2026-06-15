package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetWorkspaceImagesUseCase {
    List<WorkspaceImageResponseDto> execute(ManagerActor actor, Long workspaceId);
}
