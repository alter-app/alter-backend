package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserPostingApplicationListUseCase {
    CursorPaginatedApiResponse<UserPostingApplicationListResponseDto> execute(AppActor actor, CursorPageRequestDto pageRequest);
}
