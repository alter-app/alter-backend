package com.dreamteam.alter.application.workspace.usecase;

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

		// 같은 요일 중복 등록 검증
		if (workspaceWorkerScheduleRepository.existsByWorkspaceWorkerAndDayOfWeek(workspaceWorkerSchedule.getWorkspaceWorker(), request.getDayOfWeek()))
			throw new CustomException(ErrorCode.ALREADY_HAS_SCHEDULE_DAY);

		workspaceWorkerSchedule.update(request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

		// TODO Send FCM
	}
}
