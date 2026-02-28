package com.dreamteam.alter.adapter.outbound.auth.persistence;

import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class AuthLogRepositoryImpl implements AuthLogRepository {

    private final AuthLogJpaRepository authLogJpaRepository;

    @Override
    public AuthLog save(AuthLog authLog) {
        return authLogJpaRepository.save(authLog);
    }
}
