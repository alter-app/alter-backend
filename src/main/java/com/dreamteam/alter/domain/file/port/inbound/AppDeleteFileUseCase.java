package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppDeleteFileUseCase {
    void execute(AppActor actor, String fileId);
}
