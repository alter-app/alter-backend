package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;

public interface ReputationRequestRepository {
    void save(ReputationRequest request);
}
