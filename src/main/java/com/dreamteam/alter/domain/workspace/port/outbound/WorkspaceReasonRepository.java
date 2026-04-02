package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;

public interface WorkspaceReasonRepository {
	void save(WorkspaceReason reason);
}
