package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonCommentListResponse;

public interface WorkspaceReasonCommentQueryRepository {
	List<WorkspaceReasonCommentListResponse> getWorkspaceReasonCommentList(Long reasonId);
}
