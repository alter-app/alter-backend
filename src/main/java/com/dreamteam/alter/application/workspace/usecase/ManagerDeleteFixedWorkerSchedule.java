package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
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

		WorkspaceWorkerSchedule workspaceWorkerSchedule = workspaceWorkerScheduleQueryRepository.findById(workerScheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "삭제할 스케줄을 찾지 못하였습니다."));

		workspaceWorkerSchedule.delete();
	}
}
