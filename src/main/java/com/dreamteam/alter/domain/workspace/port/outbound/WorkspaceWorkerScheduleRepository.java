package com.dreamteam.alter.domain.workspace.port.outbound;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface WorkspaceWorkerScheduleRepository {
	void saveAll(List<WorkspaceWorkerSchedule> workspaceWorkerSchedules);

	Optional<WorkspaceWorkerSchedule> findByIdWithWorkspaceWorker(Long workerScheduleId);
	List<WorkspaceWorkerSchedule> findByWorkspaceWorkerAndDayOfWeekIn(WorkspaceWorker workspaceWorker, List<DayOfWeek> dayOfWeeks);

	boolean existsById(Long workerScheduleId);

	void deleteById(Long workerScheduleId);
}
