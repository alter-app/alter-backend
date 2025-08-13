package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToWorkspaceRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppCreateReputationToWorkspaceUseCase {
    void execute(AppActor actor, CreateReputationToWorkspaceRequestDto request);
}
