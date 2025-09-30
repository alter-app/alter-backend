package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceManagerListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetWorkspaceManagerListUseCase {
    CursorPaginatedApiResponse<ManagerWorkspaceManagerListResponseDto> execute(
        ManagerActor actor,
        Long workspaceId,
        CursorPageRequestDto pageRequest
    );
}
