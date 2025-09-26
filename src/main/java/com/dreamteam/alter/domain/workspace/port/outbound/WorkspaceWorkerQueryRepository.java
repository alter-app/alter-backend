package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceListResponse;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;

import java.util.List;
import java.util.Optional;

public interface WorkspaceWorkerQueryRepository {
    Optional<WorkspaceWorker> findActiveWorkerByWorkspaceAndUser(Workspace workspace, User user);
    
    long getUserActiveWorkspaceCount(User user);
    
    List<UserWorkspaceListResponse> getUserActiveWorkspaceListWithCursor(
        CursorPageRequest<CursorDto> request, 
        User user
    );
}
