package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingDetailResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetPostingDetailUseCase {
    PostingDetailResponseDto execute(Long postingId, AppActor actor);
}
