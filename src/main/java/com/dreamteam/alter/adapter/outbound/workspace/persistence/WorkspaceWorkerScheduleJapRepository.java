package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface WorkspaceWorkerScheduleJapRepository extends JpaRepository<WorkspaceWorkerSchedule, Long> {
	List<WorkspaceWorkerSchedule> findByWorkspaceWorkerAndDayOfWeekIn(WorkspaceWorker workspaceWorker, List<DayOfWeek> dayOfWeeks);

	@Query("SELECT wws FROM WorkspaceWorkerSchedule wws JOIN FETCH wws.workspaceWorker WHERE wws.id = :id")
	Optional<WorkspaceWorkerSchedule> findByIdWithWorkspaceWorker(@Param("id") Long id);
}
