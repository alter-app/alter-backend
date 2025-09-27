package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserWorkspaceWorkerListUseCase {
    CursorPaginatedApiResponse<UserWorkspaceWorkerListResponseDto> execute(AppActor actor, Long workspaceId, CursorPageRequestDto pageRequest);
}
