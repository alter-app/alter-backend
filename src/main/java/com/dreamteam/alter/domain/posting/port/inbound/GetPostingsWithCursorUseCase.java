package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetPostingsWithCursorUseCase {
    CursorPaginatedApiResponse<PostingListResponseDto> execute(CursorPageRequestDto request, AppActor actor);
}
