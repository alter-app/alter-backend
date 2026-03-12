package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestListFilterDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.util.List;
import java.util.Optional;

public interface BusinessJoinRequestQueryRepository {
    Optional<BusinessJoinRequest> findById(Long id);
    boolean existsPendingRequest(Workspace workspace, User user);
    long countByUser(User user, MyJoinRequestListFilterDto filter);
    long countByWorkspace(Workspace workspace, WorkspaceJoinRequestListFilterDto filter);
    List<BusinessJoinRequest> findByUserWithCursor(CursorPageRequest<CursorDto> pageRequest, User user, MyJoinRequestListFilterDto filter);
    List<BusinessJoinRequest> findByWorkspaceWithCursor(CursorPageRequest<CursorDto> pageRequest, Workspace workspace, WorkspaceJoinRequestListFilterDto filter);
}
