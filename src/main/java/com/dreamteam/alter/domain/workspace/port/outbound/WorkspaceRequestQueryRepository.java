package com.dreamteam.alter.domain.workspace.port.outbound;

public interface WorkspaceRequestQueryRepository {
	boolean existsByIdAndUserId(Long workspaceRequestId, Long userId);
}
