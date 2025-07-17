package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceQueryRepository {
    Optional<Workspace> findById(Long id);
    List<ManagerWorkspaceListResponse> getManagerWorkspaceList(ManagerUser managerUser);

    Optional<ManagerWorkspaceResponse> getByManagerUserAndId(ManagerUser managerUser, Long workspaceId);

    long getWorkspaceWorkerCount(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter
    );

    List<ManagerWorkspaceWorkerListResponse> getWorkspaceWorkerList(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        PageRequestDto pageRequest
    );
}
