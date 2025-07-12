package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;

public interface WorkspaceWorkerRepository {
    void save(WorkspaceWorker workspaceWorker);
}
