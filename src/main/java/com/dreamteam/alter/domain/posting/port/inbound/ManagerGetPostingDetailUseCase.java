package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingDetailResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPostingDetailUseCase {
    ManagerPostingDetailResponseDto execute(Long postingId, ManagerActor actor);
}
