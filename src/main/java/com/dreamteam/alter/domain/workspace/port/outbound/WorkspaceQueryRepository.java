package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceManagerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceManagerListResponse;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkspaceQueryRepository {
    Optional<Workspace> findById(Long id);
    List<ManagerWorkspaceListResponse> getManagerWorkspaceList(ManagerUser managerUser);

    ManagerWorkspaceResponse getByManagerUserAndId(ManagerUser managerUser, Long workspaceId);

    long getWorkspaceWorkerCount(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter
    );

    List<ManagerWorkspaceWorkerListResponse> getWorkspaceWorkerListWithCursor(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequest<CursorDto> pageRequest
    );

    boolean isUserActiveWorkerInWorkspace(User user, Long workspaceId);

    long getUserWorkspaceWorkerCount(Long workspaceId);

    List<UserWorkspaceWorkerListResponse> getUserWorkspaceWorkerListWithCursor(
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    );

    long getExchangeableWorkerCount(Long workspaceId, User self, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<UserWorkspaceWorkerListResponse> getExchangeableWorkerListWithCursor(
        Long workspaceId,
        User self,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        CursorPageRequest<CursorDto> pageRequest
    );

    List<Long> getExchangeableWorkerIds(Long workspaceId, User self, LocalDateTime startDateTime, LocalDateTime endDateTime);

    long getUserWorkspaceManagerCount(Long workspaceId);

    List<UserWorkspaceManagerListResponse> getUserWorkspaceManagerListWithCursor(
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    );

    long getManagerWorkspaceManagerCount(ManagerUser managerUser, Long workspaceId);

    List<ManagerWorkspaceManagerListResponse> getManagerWorkspaceManagerListWithCursor(
        ManagerUser managerUser,
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    );
}
