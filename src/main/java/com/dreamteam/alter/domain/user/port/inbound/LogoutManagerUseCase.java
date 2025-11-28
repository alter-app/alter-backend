package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface LogoutManagerUseCase {
    void execute(ManagerActor actor);
}
