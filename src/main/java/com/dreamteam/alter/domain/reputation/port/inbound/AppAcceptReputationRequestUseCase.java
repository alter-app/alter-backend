package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppAcceptReputationRequestUseCase {
    void execute(AppActor actor, Long requestId, AcceptReputationRequestDto request);
}
