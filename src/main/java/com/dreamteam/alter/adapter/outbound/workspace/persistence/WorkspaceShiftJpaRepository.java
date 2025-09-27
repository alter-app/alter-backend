package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceShiftJpaRepository extends JpaRepository<WorkspaceShift, Long> {
}
