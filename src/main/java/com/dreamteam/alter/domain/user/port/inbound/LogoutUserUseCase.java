package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface LogoutUserUseCase {
    void execute(AppActor actor);
}

