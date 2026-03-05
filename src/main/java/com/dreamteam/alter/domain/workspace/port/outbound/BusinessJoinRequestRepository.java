package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;

public interface BusinessJoinRequestRepository {
    void save(BusinessJoinRequest request);
}