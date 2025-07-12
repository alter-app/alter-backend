package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

public interface CreateWorkspaceWorkerUseCase {
    void execute(Workspace workspace, User user);
}
