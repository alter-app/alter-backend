package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerUpdatePostingUseCase {
    void execute(Long postingId, UpdatePostingCommand command, ManagerActor actor);
}
