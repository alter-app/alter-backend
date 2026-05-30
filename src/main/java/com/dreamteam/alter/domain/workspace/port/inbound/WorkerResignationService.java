package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;

public interface WorkerResignationService {
    void resign(WorkspaceWorker worker);
}
