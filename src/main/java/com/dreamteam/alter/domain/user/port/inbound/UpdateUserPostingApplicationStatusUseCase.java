package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UpdateUserPostingApplicationStatusUseCase {
    void execute(AppActor actor, Long applicationId, UpdateUserPostingApplicationStatusRequestDto request);
}
