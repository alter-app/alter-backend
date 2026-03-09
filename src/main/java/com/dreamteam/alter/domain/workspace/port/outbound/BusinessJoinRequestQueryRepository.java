package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BusinessJoinRequestQueryRepository {
    Optional<BusinessJoinRequest> findById(Long id);
    boolean existsPendingRequest(Workspace workspace, User user);
    long countByUser(User user, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to);
    long countByWorkspace(Workspace workspace, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to);
    List<BusinessJoinRequest> findByUserWithCursor(User user, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize);
    List<BusinessJoinRequest> findByWorkspaceWithCursor(Workspace workspace, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize);
}