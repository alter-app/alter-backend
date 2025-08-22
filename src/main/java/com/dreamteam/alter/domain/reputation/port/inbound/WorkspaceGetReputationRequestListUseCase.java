package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface WorkspaceGetReputationRequestListUseCase {

    CursorPaginatedApiResponse<ReputationRequestListResponseDto> execute(
        ManagerActor actor,
        ReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    );

}
