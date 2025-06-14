package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.domain.auth.entity.Authorization;

import java.util.Optional;

public interface AuthorizationRepository {
    Authorization save(Authorization authorization);
    Optional<Authorization> findById(String id);
}
