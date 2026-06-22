package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;

public interface WorkspaceRequestImageQueryRepository {
    List<WorkspaceRequestImage> findAllByWorkspaceRequestId(Long workspaceRequestId);
}
