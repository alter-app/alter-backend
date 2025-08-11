package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserCreateReputationRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UserCreateReputationUseCase {
    void execute(AppActor actor, UserCreateReputationRequestDto dto);
}
