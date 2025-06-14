package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetPostingApplicationDetailUseCase {
    PostingApplicationResponseDto execute(
        Long postingApplicationId,
        ManagerActor actor
    );
}
