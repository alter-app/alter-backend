package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;

public interface AuthorizationQueryRepository {
    List<Authorization> findAllByUser(User user);
}
