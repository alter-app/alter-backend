package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetReceivedSubstituteRequestsFilterDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.ReceivedSubstituteRequestResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetReceivedSubstituteRequestListUseCase {
    CursorPaginatedApiResponse<ReceivedSubstituteRequestResponseDto> execute(
        AppActor actor,
        GetReceivedSubstituteRequestsFilterDto filter,
        CursorPageRequestDto pageRequest
    );
}
