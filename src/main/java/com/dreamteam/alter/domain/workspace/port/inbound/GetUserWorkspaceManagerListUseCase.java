package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceManagerListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserWorkspaceManagerListUseCase {
    CursorPaginatedApiResponse<UserWorkspaceManagerListResponseDto> execute(AppActor actor, Long workspaceId, CursorPageRequestDto pageRequest);
}
