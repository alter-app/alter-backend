package com.dreamteam.alter.application.workspace.usecase;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.WorkerScheduleDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerCreateFixedWorkerSchedule implements ManagerCreateFixedWorkerScheduleUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final UserQueryRepository userQueryRepository;
	private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
	private final WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, CreateWorkerScheduleRequestDto request) {
		Workspace workspace = workspaceQueryRepository.getByIdAndManagerUser(workspaceId, actor.getManagerUser())
			.orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

		User user = userQueryRepository.findById(request.getWorkerId())
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		WorkspaceWorker workspaceWorker = workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user)
			.orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_WORKER_NOT_FOUNT));

		validOverlappingTime(workspaceWorker, request);

		workspaceWorkerScheduleRepository.saveAll(
			request.getSchedules().stream()
				.map(r -> WorkspaceWorkerSchedule.create(workspaceWorker, r.getDayOfWeek(), r.getStartTime(), r.getEndTime()))
				.toList()
		);

		// TODO Send FCM
	}

	/**
	 * 곂치는 시간 검증
	 * @param workspaceWorker 해당 근무자
	 * @param request 요청 정보
	 */
	private void validOverlappingTime(WorkspaceWorker workspaceWorker, CreateWorkerScheduleRequestDto request) {
		List<DayOfWeek> dayOfWeeks = request.getSchedules().stream()
			.map(WorkerScheduleDto::getDayOfWeek)
			.toList();

		List<WorkspaceWorkerSchedule> workspaceWorkerSchedules = workspaceWorkerScheduleQueryRepository.getByWorkspaceWorkerAndDayOfWeekIn(workspaceWorker, dayOfWeeks);

		for (WorkspaceWorkerSchedule existingSchedule : workspaceWorkerSchedules) {
			request.getSchedules().stream()
				.filter(newSchedule -> newSchedule.getDayOfWeek().equals(existingSchedule.getDayOfWeek()))
				.forEach(newSchedule -> existingSchedule.validOverlappingTime(newSchedule.getStartTime(), newSchedule.getEndTime()));
		}
	}
}
