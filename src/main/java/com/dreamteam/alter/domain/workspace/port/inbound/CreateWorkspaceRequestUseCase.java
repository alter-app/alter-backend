package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.CreateWorkspaceRequestCommand;

public interface CreateWorkspaceRequestUseCase {
	void execute(CreateWorkspaceRequestCommand command);
}
