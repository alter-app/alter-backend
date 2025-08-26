package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdatePostingStatusUseCase {
    void execute(Long postingId, UpdatePostingStatusRequestDto request, ManagerActor actor);
}
