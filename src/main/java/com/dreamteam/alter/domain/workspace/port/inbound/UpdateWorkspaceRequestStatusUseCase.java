package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

public interface UpdateWorkspaceRequestStatusUseCase {
	void execute(Long workspaceRequestId, WorkspaceRequestStatus status);
}
