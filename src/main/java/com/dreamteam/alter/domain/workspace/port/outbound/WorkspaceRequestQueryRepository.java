package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;

public interface WorkspaceRequestQueryRepository {
	boolean existsByIdAndUserId(Long workspaceRequestId, Long userId);

	List<WorkspaceRequestListResponse> getWorkspaceRequestList(Long userId);

	WorkspaceRequestResponse getWorkspaceRequest(Long userId, Long workspaceRequestId);
}
