package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.util.Optional;

public interface WorkspaceQueryRepository {
    Optional<Workspace> findById(Long id);
}
