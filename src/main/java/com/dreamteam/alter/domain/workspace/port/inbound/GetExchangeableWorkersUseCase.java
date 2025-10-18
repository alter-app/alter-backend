package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.ExchangeableWorkerResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetExchangeableWorkersUseCase {
    CursorPaginatedApiResponse<ExchangeableWorkerResponseDto> execute(
        AppActor actor, 
        Long scheduleId, 
        CursorPageRequestDto pageRequest
    );
}
