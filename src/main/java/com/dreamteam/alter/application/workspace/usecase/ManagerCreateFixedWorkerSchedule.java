package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.workspace.command.CreateFixedWorkerScheduleCommand;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service("managerCreateFixedWorkerSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerCreateFixedWorkerSchedule implements ManagerCreateFixedWorkerScheduleUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
	private final WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, CreateFixedWorkerScheduleCommand command) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		WorkspaceWorker workspaceWorker = workspaceWorkerQueryRepository.findById(command.workspaceWorkerId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장에 근무하는 사용자가 아닙니다."));

		if (!workspaceWorker.getWorkspace().getId().equals(workspaceId))
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 근무자가 아닙니다.");

		List<WorkspaceWorkerSchedule> newSchedules = command.schedules().stream()
			.map(schedule -> WorkspaceWorkerSchedule.create(workspaceWorker, schedule)).toList();
		validOverlappingTime(workspaceWorker, newSchedules);
		workspaceWorkerScheduleRepository.saveAll(newSchedules);
	}

	/**
	 * 겹치는 시간 검증
	 * @param workspaceWorker 해당 근무자
	 * @param newSchedules 새로 생성할 스케줄 목록
	 */
	private void validOverlappingTime(WorkspaceWorker workspaceWorker, List<WorkspaceWorkerSchedule> newSchedules) {
		List<WorkspaceWorkerSchedule> existingSchedules = new ArrayList<>(
			workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(workspaceWorker)
		);
		for (WorkspaceWorkerSchedule newSchedule : newSchedules) {
			for (WorkspaceWorkerSchedule existingSchedule : existingSchedules) {
				existingSchedule.validOverlappingTime(newSchedule);
			}
			existingSchedules.add(newSchedule);
		}
	}
}
