package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface BusinessTypeQueryRepository {
    List<BusinessType> findAll();

    boolean existsWorkspaceUsingBusinessType(Long businessTypeId);

    boolean existsWorkspaceRequestUsingBusinessType(Long businessTypeId);
}
