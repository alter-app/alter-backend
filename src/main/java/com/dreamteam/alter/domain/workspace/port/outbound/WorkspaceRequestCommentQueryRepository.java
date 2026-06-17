package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.model.WorkspaceRequestCommentListResponse;

public interface WorkspaceRequestCommentQueryRepository {
	List<WorkspaceRequestCommentListResponse> getCommentList(Long workspaceRequestId);
}
