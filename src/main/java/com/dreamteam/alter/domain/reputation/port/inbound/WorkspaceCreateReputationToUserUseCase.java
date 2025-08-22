package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface WorkspaceCreateReputationToUserUseCase {
    void execute(ManagerActor actor, CreateReputationToUserRequestDto request);
}
