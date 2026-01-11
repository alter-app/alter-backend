package com.dreamteam.alter.application.workspace.usecase;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerCreateWorkerSchedule implements ManagerCreateWorkerScheduleUseCase {

	private final WorkspaceRepository workspaceRepository;
	private final UserQueryRepository userQueryRepository;
	private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
	private final WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, CreateWorkerScheduleRequestDto request) {
		Workspace workspace = workspaceRepository.findByIdAndManagerUser(workspaceId, actor.getManagerUser())
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
	 * 같은 요일 곂치는 시간 검증
	 * @param workspaceWorker 해당 근무자
	 * @param request 요청 정보
	 */
	private void validOverlappingTime(WorkspaceWorker workspaceWorker, CreateWorkerScheduleRequestDto request) {
		List<DayOfWeek> dayOfWeeks = request.getSchedules().stream()
			.map(CreateWorkerScheduleRequestDto.WorkerScheduleDto::getDayOfWeek)
			.toList();

		List<WorkspaceWorkerSchedule> workspaceWorkerSchedules = workspaceWorkerScheduleRepository.findByWorkspaceWorkerAndDayOfWeekIn(workspaceWorker, dayOfWeeks);

		for (WorkspaceWorkerSchedule existingSchedule : workspaceWorkerSchedules) {
			request.getSchedules().stream()
				.filter(newSchedule -> newSchedule.getDayOfWeek().equals(existingSchedule.getDayOfWeek()))
				.forEach(newSchedule -> existingSchedule.validOverlappingTime(newSchedule.getStartTime(), newSchedule.getEndTime()));
		}
	}
}
