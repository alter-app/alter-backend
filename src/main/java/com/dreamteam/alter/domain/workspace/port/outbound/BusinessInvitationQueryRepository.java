package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BusinessInvitationQueryRepository {
    Optional<BusinessInvitation> findById(Long id);
    Set<Long> findPendingInvitedUserIdsByUserIds(Long workspaceId, Set<Long> userIds);
    long countByUser(User user, BusinessInvitationStatus status, LocalDateTime from, LocalDateTime to);
    List<BusinessInvitation> findByUserWithCursor(User user, BusinessInvitationStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize);
}
