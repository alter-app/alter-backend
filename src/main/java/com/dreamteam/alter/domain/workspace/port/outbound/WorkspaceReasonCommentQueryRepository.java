package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;

public interface WorkspaceReasonCommentQueryRepository {
	List<WorkspaceReasonComment> getCommentsByReasonId(Long reasonId);
}
