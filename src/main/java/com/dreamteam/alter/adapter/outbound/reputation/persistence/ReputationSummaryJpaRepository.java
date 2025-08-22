package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationSummaryJpaRepository extends JpaRepository<ReputationSummary, Long> {
}
