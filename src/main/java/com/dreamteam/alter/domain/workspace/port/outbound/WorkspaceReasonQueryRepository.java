package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;
import java.util.Optional;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonListResponse;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;

public interface WorkspaceReasonQueryRepository {
    Optional<WorkspaceReason> findByIdAndWorkspaceRequestId(Long reasonId, Long workspaceRequestId);

    List<WorkspaceReasonListResponse> getWorkspaceReasonList(Long workspaceRequestId);
}
