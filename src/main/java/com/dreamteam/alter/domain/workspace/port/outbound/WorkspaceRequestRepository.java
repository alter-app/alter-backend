package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;

public interface WorkspaceRequestRepository {
	Long save(WorkspaceRequest workspaceRequest);
}
