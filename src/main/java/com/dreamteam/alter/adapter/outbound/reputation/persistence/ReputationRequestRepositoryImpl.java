package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReputationRequestRepositoryImpl implements ReputationRequestRepository {

    private final ReputationRequestJpaRepository reputationRequestJpaRepository;

    @Override
    public void save(ReputationRequest request) {
        reputationRequestJpaRepository.save(request);
    }

}
