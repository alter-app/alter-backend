package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;

public interface WorkspaceReasonCommentRepository {
    WorkspaceReasonComment save(WorkspaceReasonComment comment);
}
