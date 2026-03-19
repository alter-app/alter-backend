package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.Workspace;

public interface WorkspaceRepository {
	void save(Workspace workspace);
}
