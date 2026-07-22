package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.Optional;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface BusinessTypeRepository {
    Optional<BusinessType> findById(Long id);

    BusinessType save(BusinessType businessType);

    void delete(BusinessType businessType);

    boolean existsByName(String name);
}
