package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPostingListUseCase {
    CursorPaginatedApiResponse<ManagerPostingListResponseDto> execute(CursorPageRequestDto request, ManagerPostingListFilterDto filter, ManagerActor actor);
}
