package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.Optional;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;

public interface WorkspaceReasonQueryRepository {
    Optional<WorkspaceReason> findByIdAndWorkspaceRequestId(Long reasonId, Long workspaceRequestId);
}
