package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestComment;

public interface WorkspaceRequestCommentRepository {
	WorkspaceRequestComment save(WorkspaceRequestComment comment);
}
