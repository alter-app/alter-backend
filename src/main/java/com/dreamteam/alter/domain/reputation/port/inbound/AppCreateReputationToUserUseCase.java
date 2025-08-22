package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppCreateReputationToUserUseCase {
    void execute(AppActor actor, CreateReputationToUserRequestDto dto);
}
