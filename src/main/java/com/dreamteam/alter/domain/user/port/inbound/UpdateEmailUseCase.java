package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.RegisterEmailRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UpdateEmailUseCase {
    void execute(AppActor actor, RegisterEmailRequestDto request);
}
