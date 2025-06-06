package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingApplicationRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreatePostingApplicationUseCase {
    void execute(AppActor actor, Long postingId, CreatePostingApplicationRequestDto request);
}
