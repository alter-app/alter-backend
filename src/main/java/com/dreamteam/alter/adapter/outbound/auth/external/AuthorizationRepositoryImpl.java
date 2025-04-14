package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class AuthorizationRepositoryImpl implements AuthorizationRepository {

    private final AuthorizationJpaRepository authorizationJpaRepository;

    @Override
    public void save(Authorization authorization) {
        authorizationJpaRepository.save(authorization);
    }

}
