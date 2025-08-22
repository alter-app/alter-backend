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
    public void save(ReputationSummary reputationSummary) {
        reputationSummaryJpaRepository.save(reputationSummary);
    }

    @Override
    public void saveAll(Iterable<ReputationSummary> reputationSummaries) {
        reputationSummaryJpaRepository.saveAll(reputationSummaries);
    }

    @Override
    public void delete(ReputationSummary reputationSummary) {
        reputationSummaryJpaRepository.delete(reputationSummary);
    }
    
}
