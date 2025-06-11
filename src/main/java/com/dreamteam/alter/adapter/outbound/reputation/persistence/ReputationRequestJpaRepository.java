package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationRequestJpaRepository extends JpaRepository<ReputationRequest, Long> {
}
