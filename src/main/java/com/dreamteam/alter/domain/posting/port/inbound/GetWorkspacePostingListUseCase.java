package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

import java.util.List;

public interface GetWorkspacePostingListUseCase {
    List<PostingListResponseDto> execute(Long workspaceId, AppActor actor);
}
