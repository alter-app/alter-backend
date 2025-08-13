package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

import java.time.LocalDateTime;
import java.util.List;

public interface ReputationRequestQueryRepository {
    List<ReputationRequest> findAllByStatusAndExpiredAtBefore(ReputationRequestStatus status, LocalDateTime now);

    ReputationRequest findByTargetAndId(ReputationType targetType, Long targetId, Long requestId);

    long getCountOfReputationRequestsByTarget(ReputationType targetType, Long targetId);

    List<ReputationRequestListResponse> getReputationRequestsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        ReputationType targetType,
        Long targetId
    );
}
