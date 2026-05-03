package com.dreamteam.alter.domain.user.port.outbound;

import java.util.Optional;

import com.dreamteam.alter.domain.user.entity.ManagerUser;

public interface ManagerUserQueryRepository {
    Optional<ManagerUser> findByUserId(Long userId);

    Optional<ManagerUser> findById(Long id);
}
