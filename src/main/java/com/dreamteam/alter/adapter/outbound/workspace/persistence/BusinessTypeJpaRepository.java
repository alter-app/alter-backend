package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface BusinessTypeJpaRepository extends JpaRepository<BusinessType, Long> {
    boolean existsByName(String name);
}
