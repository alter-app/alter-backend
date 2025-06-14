package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdatePostingApplicationStatusUseCase {
    void execute(
        Long postingApplicationId,
        UpdatePostingApplicationStatusRequestDto request,
        ManagerActor actor
    );
}
