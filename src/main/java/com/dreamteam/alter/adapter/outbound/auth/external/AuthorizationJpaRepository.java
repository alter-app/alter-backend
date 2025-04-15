package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationJpaRepository extends JpaRepository<Authorization, String> {
}
