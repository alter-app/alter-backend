package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceWorkerJpaRepository extends JpaRepository<WorkspaceWorker, Long> {
}
