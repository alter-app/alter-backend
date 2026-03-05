package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessJoinRequestJpaRepository extends JpaRepository<BusinessJoinRequest, Long> {
}
