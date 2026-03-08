package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;

import java.time.LocalDate;

public interface GetWorkspaceJoinRequestListUseCase {
    CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto> execute(ManagerActor actor, Long workspaceId, BusinessJoinRequestStatus status, LocalDate from, LocalDate to, CursorPageRequestDto cursorPageRequest);
}
