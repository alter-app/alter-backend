package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface CreatePostingUseCase {
    void execute(CreatePostingCommand command, ManagerActor actor);
}
