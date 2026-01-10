package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

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
}
