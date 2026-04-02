package com.dreamteam.alter.domain.workspace.port.inbound;

public interface CreateWorkspaceReasonUseCase {
	void execute(Long workspaceRequestId, String reason);
}
