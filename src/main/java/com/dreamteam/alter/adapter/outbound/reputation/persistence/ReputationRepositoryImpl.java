package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReputationRepositoryImpl implements ReputationRepository {

    private final ReputationJpaRepository reputationJpaRepository;

    @Override
    public Reputation save(Reputation reputation) {
        return reputationJpaRepository.save(reputation);
    }

}
