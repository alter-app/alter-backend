package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.util.List;
import java.util.Optional;

public interface BusinessJoinRequestQueryRepository {
    Optional<BusinessJoinRequest> findById(Long id);
    boolean existsPendingRequest(Workspace workspace, User user);
    List<BusinessJoinRequest> findPendingByWorkspace(Workspace workspace);
    List<BusinessJoinRequest> findPendingByUser(User user);
}