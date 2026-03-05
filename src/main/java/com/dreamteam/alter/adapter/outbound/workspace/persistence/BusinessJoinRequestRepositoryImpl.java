package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessJoinRequestRepositoryImpl implements BusinessJoinRequestRepository {

    private final BusinessJoinRequestJpaRepository businessJoinRequestJpaRepository;

    @Override
    public void save(BusinessJoinRequest request) {
        businessJoinRequestJpaRepository.save(request);
    }
}
