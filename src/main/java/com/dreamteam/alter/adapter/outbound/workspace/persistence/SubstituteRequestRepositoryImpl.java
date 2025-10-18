package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubstituteRequestRepositoryImpl implements SubstituteRequestRepository {

    private final SubstituteRequestJpaRepository substituteRequestJpaRepository;

    @Override
    public void save(SubstituteRequest substituteRequest) {
        substituteRequestJpaRepository.save(substituteRequest);
    }
}

