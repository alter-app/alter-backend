package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerScheduleRepositoryImpl implements WorkspaceWorkerScheduleRepository {

	private final WorkspaceWorkerScheduleJapRepository workspaceWorkerScheduleJapRepository;

	@Override
	public void saveAll(List<WorkspaceWorkerSchedule> workspaceWorkerSchedules) {
		workspaceWorkerScheduleJapRepository.saveAll(workspaceWorkerSchedules);
	}

	@Override
	public boolean existsById(Long workerScheduleId) {
		return workspaceWorkerScheduleJapRepository.existsById(workerScheduleId);
	}

	@Override
	public boolean existsByWorkspaceWorkerAndDayOfWeekIn(WorkspaceWorker workspaceWorker, List<DayOfWeek> dayOfWeeks) {
		return workspaceWorkerScheduleJapRepository.existsByWorkspaceWorkerAndDayOfWeekIn(workspaceWorker, dayOfWeeks);
	}

	@Override
	public Optional<WorkspaceWorkerSchedule> findById(Long workerScheduleId) {
		return workspaceWorkerScheduleJapRepository.findById(workerScheduleId);
	}

	@Override
	public void deleteById(Long workerScheduleId) {
		workspaceWorkerScheduleJapRepository.deleteById(workerScheduleId);
	}
}
