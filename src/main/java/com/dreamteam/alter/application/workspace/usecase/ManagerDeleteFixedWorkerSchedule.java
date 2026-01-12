package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerDeleteFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerDeleteFixedWorkerSchedule implements ManagerDeleteFixedWorkerScheduleUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		if (!workspaceWorkerScheduleQueryRepository.existsById(workerScheduleId))
			throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND_FOR_DELETE);

		workspaceWorkerScheduleQueryRepository.deleteById(workerScheduleId);

		// TODO Send FCM
	}
}
