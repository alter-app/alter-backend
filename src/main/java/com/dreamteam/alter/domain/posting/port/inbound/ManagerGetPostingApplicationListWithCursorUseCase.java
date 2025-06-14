package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPostingApplicationListWithCursorUseCase {
    CursorPaginatedApiResponse<PostingApplicationListResponseDto> execute(
        CursorPageRequestDto cursorPageRequest,
        PostingApplicationListFilterDto filter,
        ManagerActor actor
    );
}
