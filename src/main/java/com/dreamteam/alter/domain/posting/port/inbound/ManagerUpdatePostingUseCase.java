package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdatePostingUseCase {
    void execute(Long postingId, UpdatePostingRequestDto request, ManagerActor actor);
}
