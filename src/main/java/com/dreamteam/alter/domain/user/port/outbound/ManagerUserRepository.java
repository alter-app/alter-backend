package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.ManagerUser;

public interface ManagerUserRepository {
    ManagerUser save(ManagerUser managerUser);
}
