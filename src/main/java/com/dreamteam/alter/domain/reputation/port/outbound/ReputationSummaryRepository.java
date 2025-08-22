package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;

public interface ReputationSummaryRepository {
    
    void save(ReputationSummary reputationSummary);

    void saveAll(Iterable<ReputationSummary> reputationSummaries);

    void delete(ReputationSummary reputationSummary);
    
}
