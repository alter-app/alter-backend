package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetSubstituteRequestListUseCase {
    CursorPaginatedApiResponse<ManagerSubstituteRequestResponseDto> execute(
        ManagerActor actor,
        ManagerSubstituteRequestListFilterDto filter,
        CursorPageRequestDto pageRequest
    );
}
