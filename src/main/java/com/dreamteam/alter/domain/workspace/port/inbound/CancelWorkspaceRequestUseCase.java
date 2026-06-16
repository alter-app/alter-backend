package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface CancelWorkspaceRequestUseCase {
	void execute(User user, Long workspaceRequestId);
}
