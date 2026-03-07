package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateFixedScheduleDateUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("managerUpdateFixedScheduleDate")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateFixedScheduleDate implements ManagerUpdateFixedScheduleDateUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;

	@Override
	public void execute(ManagerActor actor, Long workspaceId, int nextMonthShiftGenDay) {
		if (workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
			throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
		}

		Workspace workspace = workspaceQueryRepository.findById(workspaceId)
			.orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

		workspace.updateNextMonthShiftGenDay(nextMonthShiftGenDay);
	}
}
