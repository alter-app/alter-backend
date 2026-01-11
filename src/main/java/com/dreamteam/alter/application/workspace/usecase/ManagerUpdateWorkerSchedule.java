package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateWorkerSchedule implements ManagerUpdateWorkerScheduleUseCase {

	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId, UpdateWorkerScheduleRequestDto request) {
		if (!workspaceRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		WorkspaceWorkerSchedule workspaceWorkerSchedule = workspaceWorkerScheduleRepository.findById(workerScheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND_FOR_UPDATE));

		validOverlappingTime(workspaceWorkerSchedule, request);

		workspaceWorkerSchedule.update(request.getStartTime(), request.getEndTime());

		// TODO Send FCM
	}

	/**
	 * 같은 요일 곂치는 시간 검증 (자기 자신 제외)
	 * @param workspaceWorkerSchedule 수정 중인 스케줄
	 * @param request 요청 정보
	 */
	private void validOverlappingTime(WorkspaceWorkerSchedule workspaceWorkerSchedule, UpdateWorkerScheduleRequestDto request) {
		List<WorkspaceWorkerSchedule> existingSchedules = workspaceWorkerScheduleRepository.findByWorkspaceWorkerAndDayOfWeekIn(workspaceWorkerSchedule.getWorkspaceWorker(), List.of(workspaceWorkerSchedule.getDayOfWeek()));

		existingSchedules.stream()
			.filter(schedule -> !schedule.getId().equals(workspaceWorkerSchedule.getId()))
			.forEach(schedule -> schedule.validOverlappingTime(request.getStartTime(), request.getEndTime()));
	}
}
