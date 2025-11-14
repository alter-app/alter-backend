package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.ManagerSelfInfoResponse;
import com.dreamteam.alter.domain.user.entity.ManagerUser;

import java.util.Optional;

public interface ManagerUserQueryRepository {
    Optional<ManagerUser> findByUserId(Long userId);

    Optional<ManagerSelfInfoResponse> getManagerSelfInfoSummary(Long managerId);

    Optional<ManagerUser> findById(Long id);
}
