package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserFavoritePostingListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserFavoritePostingListUseCase {
    CursorPaginatedApiResponse<UserFavoritePostingListResponseDto> execute(AppActor actor, CursorPageRequestDto pageRequest);
}
