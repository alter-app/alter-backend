package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationListFilterDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BusinessInvitationQueryRepository {
    Optional<BusinessInvitation> findById(Long id);
    Set<Long> findPendingInvitedUserIdsByUserIds(Long workspaceId, Set<Long> userIds);
    long countByUser(User user, MyInvitationListFilterDto filter);
    List<BusinessInvitation> findByUserWithCursor(CursorPageRequest<CursorDto> pageRequest, User user, MyInvitationListFilterDto filter);
}
