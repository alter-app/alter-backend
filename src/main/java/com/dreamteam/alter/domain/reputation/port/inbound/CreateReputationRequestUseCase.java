package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateReputationRequestUseCase {
    void execute(AppActor actor, CreateReputationRequestDto request);
}
