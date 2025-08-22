package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReputationSummaryRepositoryImpl implements ReputationSummaryRepository {
    
    private final ReputationSummaryJpaRepository reputationSummaryJpaRepository;
    
    @Override
    public ReputationSummary save(ReputationSummary reputationSummary) {
        return reputationSummaryJpaRepository.save(reputationSummary);
    }

    @Override
    public void delete(ReputationSummary reputationSummary) {
        reputationSummaryJpaRepository.delete(reputationSummary);
    }
    
}
