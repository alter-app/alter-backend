package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerDeleteFileUseCase {
    void execute(ManagerActor actor, String fileId);
}
