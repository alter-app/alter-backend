package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetWorkspaceWorkerListUseCase {
    CursorPaginatedApiResponse<ManagerWorkspaceWorkerListResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequestDto pageRequest
    );
}
