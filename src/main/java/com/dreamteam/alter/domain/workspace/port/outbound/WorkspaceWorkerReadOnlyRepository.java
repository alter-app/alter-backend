package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;

import java.util.Optional;

public interface WorkspaceWorkerReadOnlyRepository {
    Optional<WorkspaceWorker> findActiveWorkerByWorkspaceAndUser(Workspace workspace, User user);
}
