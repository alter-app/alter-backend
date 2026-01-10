package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface WorkspaceWorkerScheduleJapRepository extends JpaRepository<WorkspaceWorkerSchedule, Long> {
}
