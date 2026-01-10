package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

public interface WorkspaceJpaRepository extends JpaRepository<Workspace, Long> {
	Optional<Workspace> findByIdAndManagerUser(Long id, ManagerUser managerUser);

	boolean existsByIdAndManagerUser(Long id, ManagerUser managerUser);
}
