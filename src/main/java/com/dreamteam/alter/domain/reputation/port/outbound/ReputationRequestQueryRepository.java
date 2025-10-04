package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

import java.time.LocalDateTime;
import java.util.List;

public interface ReputationRequestQueryRepository {
    List<ReputationRequest> findAllByStatusAndExpiredAtBefore(ReputationRequestStatus status, LocalDateTime now);

    ReputationRequest findById(Long requestId);

    ReputationRequest findByTargetAndId(ReputationType targetType, Long targetId, Long requestId);

    long getCountOfReputationRequestsByUser(Long userId, ReputationRequestStatus status);

    List<ReputationRequestListResponse> getReputationRequestsWithCursorByUser(
        CursorPageRequest<CursorDto> pageRequest,
        Long userId,
        ReputationRequestStatus status
    );

    long getCountOfReputationRequestsByWorkspace(List<Long> workspaceIds, ReputationRequestFilterDto filter);

    List<ReputationRequestListResponse> getReputationRequestsWithCursorByWorkspace(
        CursorPageRequest<CursorDto> pageRequest,
        List<Long> workspaceIds,
        ReputationRequestFilterDto filter
    );

    long getCountOfSentReputationRequestsByUser(Long userId, ReputationRequestStatus status);

    List<ReputationRequestListResponse> getSentReputationRequestsWithCursorByUser(
        CursorPageRequest<CursorDto> pageRequest,
        Long userId,
        ReputationRequestStatus status
    );

    long getCountOfSentReputationRequestsByManager(Long managerId, ReputationRequestStatus status);

    List<ReputationRequestListResponse> getSentReputationRequestsWithCursorByManager(
        CursorPageRequest<CursorDto> pageRequest,
        Long managerId,
        ReputationRequestStatus status
    );

    ReputationRequest findSentReputationRequestByUser(Long userId, Long requestId);

    ReputationRequest findSentReputationRequestByManager(Long managerId, Long requestId);
}
