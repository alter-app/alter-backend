package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerUserJpaRepository extends JpaRepository<ManagerUser, Long> {
}
