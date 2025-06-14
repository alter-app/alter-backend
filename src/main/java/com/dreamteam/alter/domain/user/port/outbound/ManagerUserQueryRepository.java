package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.ManagerUser;

import java.util.Optional;

public interface ManagerUserQueryRepository {
    Optional<ManagerUser> findByUserId(Long userId);
}
