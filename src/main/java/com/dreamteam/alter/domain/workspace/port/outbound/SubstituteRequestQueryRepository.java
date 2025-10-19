package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ReceivedSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestDetailResponse;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubstituteRequestQueryRepository {
    Optional<SubstituteRequest> findById(Long id);
    Optional<SubstituteRequest> findByIdWithPessimisticLock(Long id);
    
    boolean existsActiveRequestByScheduleAndRequester(Long scheduleId, Long requesterId);

    List<SubstituteRequest> findAllByStatusAndExpiresAtBefore(SubstituteRequestStatus status, LocalDateTime expiresAt);

    long getReceivedRequestCount(User user, Long workspaceId);
    List<ReceivedSubstituteRequestListResponse> getReceivedRequestListWithCursor(
        User user, 
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    );
    
    long getSentRequestCount(User user, SubstituteRequestStatus status);
    List<SentSubstituteRequestListResponse> getSentRequestListWithCursor(
        User user,
        SubstituteRequestStatus status,
        CursorPageRequest<CursorDto> pageRequest
    );
    
    long getManagerRequestCount(Long workspaceId, SubstituteRequestStatus status);
    List<ManagerSubstituteRequestListResponse> getManagerRequestListWithCursor(
        Long workspaceId,
        SubstituteRequestStatus status,
        CursorPageRequest<CursorDto> pageRequest
    );
    
    Optional<SentSubstituteRequestDetailResponse> getSentRequestDetail(User user, Long requestId);
}

