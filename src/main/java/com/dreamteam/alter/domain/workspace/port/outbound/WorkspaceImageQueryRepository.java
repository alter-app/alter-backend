package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;

public interface WorkspaceImageQueryRepository {
    List<WorkspaceImage> findAllByWorkspaceId(Long workspaceId);
    long countByWorkspaceId(Long workspaceId);
}
