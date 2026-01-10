package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.Optional;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

public interface WorkspaceRepository {
	Optional<Workspace> findByIdAndManagerUser(Long workspaceId, ManagerUser managerUser);
	boolean existsByIdAndManagerUser(Long workspaceId, ManagerUser managerUser);
}
