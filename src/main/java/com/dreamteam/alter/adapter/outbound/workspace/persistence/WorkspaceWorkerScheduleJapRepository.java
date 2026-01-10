package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface WorkspaceWorkerScheduleJapRepository extends JpaRepository<WorkspaceWorkerSchedule, Long> {
	boolean existsByWorkspaceWorkerAndDayOfWeek(WorkspaceWorker workspaceWorker, DayOfWeek dayOfWeek);
	boolean existsByWorkspaceWorkerAndDayOfWeekIn(WorkspaceWorker workspaceWorker, List<DayOfWeek> dayOfWeeks);
}
