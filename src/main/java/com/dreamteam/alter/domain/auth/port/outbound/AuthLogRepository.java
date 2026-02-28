package com.dreamteam.alter.domain.auth.port.outbound;

import com.dreamteam.alter.domain.auth.entity.AuthLog;

public interface AuthLogRepository {
    AuthLog save(AuthLog authLog);
}
