package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdatePasswordRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface UpdatePasswordUseCase {
    void execute(AppActor actor, UpdatePasswordRequestDto request);
}
