package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestListRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestListResponseDto;

public interface GetReputationRequestListUseCase {

    CursorPaginatedApiResponse<ReputationRequestListResponseDto> execute(
        ReputationRequestListRequestDto request,
        CursorPageRequestDto pageRequest
    );

}
