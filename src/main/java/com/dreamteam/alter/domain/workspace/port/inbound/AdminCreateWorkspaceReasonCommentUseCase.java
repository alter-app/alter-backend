package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminCreateWorkspaceReasonCommentUseCase {
	void execute(AdminActor actor, Long workspaceRequestId, Long reasonId, String comment);
}
