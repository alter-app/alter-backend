package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.User;

public interface UserRepository {
    User save(User user);
}
