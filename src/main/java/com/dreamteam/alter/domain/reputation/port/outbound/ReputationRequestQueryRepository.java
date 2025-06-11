package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReputationRequestQueryRepository {
    List<ReputationRequest> findAllByStatusAndExpiredAtBefore(ReputationRequestStatus status, LocalDateTime now);
}
