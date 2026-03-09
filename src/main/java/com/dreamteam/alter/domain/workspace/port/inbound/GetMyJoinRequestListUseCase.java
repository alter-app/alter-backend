package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetMyJoinRequestListUseCase {
    CursorPaginatedApiResponse<MyJoinRequestResponseDto> execute(AppActor actor, MyJoinRequestListFilterDto filter, CursorPageRequestDto cursorPageRequest);
}
