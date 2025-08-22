package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;

public interface ReputationSummaryRepository {
    
    ReputationSummary save(ReputationSummary reputationSummary);
    
    void delete(ReputationSummary reputationSummary);
    
}
