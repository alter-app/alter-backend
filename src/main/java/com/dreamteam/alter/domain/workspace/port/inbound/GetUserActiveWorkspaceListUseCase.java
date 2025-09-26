package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserActiveWorkspaceListUseCase {
    CursorPaginatedApiResponse<UserWorkspaceListResponseDto> execute(
        CursorPageRequestDto request, 
        AppActor actor
    );
}

