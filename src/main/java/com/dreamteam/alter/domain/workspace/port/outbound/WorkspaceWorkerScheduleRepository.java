package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;

public interface WorkspaceWorkerScheduleRepository {
	void saveAll(List<WorkspaceWorkerSchedule> workspaceWorkerSchedules);
}
