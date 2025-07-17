package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

import java.util.List;

public interface ManagerGetWorkspaceListUseCase {
    List<ManagerWorkspaceListResponseDto> execute(ManagerActor actor);
}
