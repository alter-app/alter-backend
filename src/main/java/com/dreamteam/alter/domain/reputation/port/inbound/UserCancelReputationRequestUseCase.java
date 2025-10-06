package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface UserCancelReputationRequestUseCase {
    void execute(AppActor actor, Long requestId);
}
