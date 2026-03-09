package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface GetWorkspaceJoinRequestListUseCase {
    CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId, WorkspaceJoinRequestListFilterDto filter, CursorPageRequestDto cursorPageRequest);
}
