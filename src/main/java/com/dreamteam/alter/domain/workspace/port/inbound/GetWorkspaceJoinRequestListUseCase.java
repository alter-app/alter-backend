package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

import java.util.List;

public interface GetWorkspaceJoinRequestListUseCase {
    List<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId);
}
