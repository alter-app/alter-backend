package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ManagerUserRepositoryImpl implements ManagerUserRepository {

    private final ManagerUserJpaRepository managerUserJpaRepository;

    @Override
    public ManagerUser save(ManagerUser managerUser) {
        return managerUserJpaRepository.save(managerUser);
    }
}
