package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateFixedWorkerSchedule implements ManagerUpdateFixedWorkerScheduleUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId, UpdateWorkerScheduleRequestDto request) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		WorkspaceWorkerSchedule workspaceWorkerSchedule = workspaceWorkerScheduleQueryRepository.getByIdWithWorkspaceWorker(workerScheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "수정할 스케줄을 찾을 수 없습니다."));

		validOverlappingTime(workspaceWorkerSchedule, request);

		workspaceWorkerSchedule.update(request.getStartTime(), request.getEndTime());
	}

	/**
	 * 같은 요일 곂치는 시간 검증 (자기 자신 제외)
	 * @param workspaceWorkerSchedule 수정 중인 스케줄
	 * @param request 요청 정보
	 */
	private void validOverlappingTime(WorkspaceWorkerSchedule workspaceWorkerSchedule, UpdateWorkerScheduleRequestDto request) {
		List<WorkspaceWorkerSchedule> existingSchedules = workspaceWorkerScheduleQueryRepository.getByWorkspaceWorkerAndDayOfWeekIn(workspaceWorkerSchedule.getWorkspaceWorker(), List.of(workspaceWorkerSchedule.getDayOfWeek()));

		existingSchedules.stream()
			.filter(schedule -> !schedule.getId().equals(workspaceWorkerSchedule.getId()))
			.forEach(schedule -> schedule.validOverlappingTime(request.getStartTime(), request.getEndTime()));
	}
}
