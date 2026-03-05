package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ApproveJoinRequestUseCase {
    void execute(ManagerActor actor, Long workspaceId, Long requestId);
}
