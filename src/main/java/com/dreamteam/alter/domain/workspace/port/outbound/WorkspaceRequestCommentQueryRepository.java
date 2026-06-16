package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestCommentListResponse;

public interface WorkspaceRequestCommentQueryRepository {
	List<WorkspaceRequestCommentListResponse> getCommentList(Long workspaceRequestId);
}
