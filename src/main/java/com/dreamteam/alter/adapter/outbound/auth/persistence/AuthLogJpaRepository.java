package com.dreamteam.alter.adapter.outbound.auth.persistence;

import com.dreamteam.alter.domain.auth.entity.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogJpaRepository extends JpaRepository<AuthLog, Long> {
}
