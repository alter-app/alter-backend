package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetPostingMapListUseCase {
    CursorPaginatedApiResponse<PostingMapListResponseDto> execute(CursorPageRequestDto request, PostingMapListFilterDto filter, AppActor actor);
}
