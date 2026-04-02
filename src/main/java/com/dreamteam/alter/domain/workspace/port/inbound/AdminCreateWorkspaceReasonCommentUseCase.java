package com.dreamteam.alter.domain.workspace.port.inbound;

public interface AdminCreateWorkspaceReasonCommentUseCase {
	void execute(Long workspaceRequestId, Long reasonId, String comment);
}
