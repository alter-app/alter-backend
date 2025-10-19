package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface CancelSubstituteRequestUseCase {
    void execute(AppActor actor, Long requestId);
}
