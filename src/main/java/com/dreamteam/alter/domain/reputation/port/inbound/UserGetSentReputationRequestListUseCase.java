package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserReputationRequestFilterDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UserGetSentReputationRequestListUseCase {
    CursorPaginatedApiResponse<ReputationRequestListResponseDto> execute(
        AppActor actor,
        UserReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    );
}
