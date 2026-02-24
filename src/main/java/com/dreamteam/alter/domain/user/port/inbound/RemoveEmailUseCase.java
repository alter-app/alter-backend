package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface RemoveEmailUseCase {
    void execute(AppActor actor);
}
