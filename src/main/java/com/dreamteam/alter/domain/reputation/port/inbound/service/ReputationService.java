package com.dreamteam.alter.domain.reputation.port.inbound.service;

public interface ReputationService {
    void expireReputationRequests();
    void updateReputationSummaries();
    void cleanupInactiveReputationSummaries();
}
