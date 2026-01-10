package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerDeleteWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerDeleteWorkerSchedule implements ManagerDeleteWorkerScheduleUseCase {

	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, Long workerScheduleId) {
		if (!workspaceRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		if (!workspaceWorkerScheduleRepository.existsById(workerScheduleId))
			throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND_FOR_DELETE);

		workspaceWorkerScheduleRepository.deleteById(workerScheduleId);

		// TODO Send FCM
	}
}
