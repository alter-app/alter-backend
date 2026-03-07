package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.FixedWorkerScheduleResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetFixedWorkerScheduleListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetFixedWorkerScheduleList implements ManagerGetFixedWorkerScheduleListUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

	@Override
	public List<FixedWorkerScheduleResponseDto> execute(ManagerActor actor, Long workspaceId) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser()))
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);

		List<WorkspaceWorkerSchedule> schedules = workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceId(workspaceId);

		return schedules.stream()
			.map(FixedWorkerScheduleResponseDto::of)
			.toList();
	}
}
