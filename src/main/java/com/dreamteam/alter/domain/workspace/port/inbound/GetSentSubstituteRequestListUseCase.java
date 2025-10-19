package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.SubstituteRequestListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;

public interface GetSentSubstituteRequestListUseCase {
    CursorPaginatedApiResponse<SubstituteRequestListResponseDto> execute(
        AppActor actor,
        SubstituteRequestStatus status,
        CursorPageRequestDto pageRequest
    );
}
