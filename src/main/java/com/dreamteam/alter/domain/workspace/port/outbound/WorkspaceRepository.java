package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.Optional;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

public interface WorkspaceRepository {
	Optional<Workspace> getByIdAndManagerUser(Long workspaceId, ManagerUser managerUser);
}
