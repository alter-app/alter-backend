package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminDeleteFileUseCase {
    void execute(AdminActor actor, String fileId);
}
