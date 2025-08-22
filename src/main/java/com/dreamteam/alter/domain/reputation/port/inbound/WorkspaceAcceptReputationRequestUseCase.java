package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface WorkspaceAcceptReputationRequestUseCase {
    void execute(ManagerActor actor, Long requestId, AcceptReputationRequestDto request);
}
