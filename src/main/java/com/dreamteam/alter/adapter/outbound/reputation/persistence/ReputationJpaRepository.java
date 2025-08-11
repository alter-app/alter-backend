package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.Reputation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationJpaRepository extends JpaRepository<Reputation, Long> {
}
