package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.domain.auth.entity.Authorization;

public interface AuthorizationRepository {
    void save(Authorization authorization);
}
