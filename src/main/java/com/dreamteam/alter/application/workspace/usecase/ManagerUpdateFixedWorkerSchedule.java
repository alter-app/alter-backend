package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service("managerUpdateFixedWorkerSchedule")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateFixedWorkerSchedule implements ManagerUpdateFixedWorkerScheduleUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId, FixedWorkerScheduleCommand command) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		WorkspaceWorkerSchedule workspaceWorkerSchedule = workspaceWorkerScheduleQueryRepository.getByIdWithWorkspaceWorker(workerScheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "수정할 스케줄을 찾을 수 없습니다."));

		if (!workspaceWorkerSchedule.getWorkspaceWorker().getWorkspace().getId().equals(workspaceId))
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 고정 근무가 아닙니다.");

		validOverlappingTime(workspaceWorkerSchedule, workspaceWorkerSchedule.updatedSchedule(command));
		workspaceWorkerSchedule.update(command);
	}

	/**
	 * 겹치는 시간 검증 (자기 자신 제외)
	 * @param workspaceWorkerSchedule 수정 중인 스케줄
	 * @param candidate 변경할 스케줄
	 */
	private void validOverlappingTime(WorkspaceWorkerSchedule workspaceWorkerSchedule, WorkspaceWorkerSchedule candidate) {
		List<WorkspaceWorkerSchedule> existingSchedules = workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(workspaceWorkerSchedule.getWorkspaceWorker());

		existingSchedules.stream()
			.filter(schedule -> !schedule.getId().equals(workspaceWorkerSchedule.getId()))
			.forEach(schedule -> schedule.validOverlappingTime(candidate));
	}
}
