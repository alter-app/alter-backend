package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonCommentsUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceReasonComments")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceReasonComments implements GetWorkspaceReasonCommentsUseCase {

	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
	private final WorkspaceReasonCommentQueryRepository workspaceReasonCommentQueryRepository;

	@Override
	public List<WorkspaceReasonCommentResponseDto> execute(ManagerActor actor, Long workspaceId, Long reasonId) {
		if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		workspaceReasonQueryRepository.findByIdAndWorkspaceId(reasonId, workspaceId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		return workspaceReasonCommentQueryRepository.getCommentsByReasonId(reasonId)
			.stream()
			.map(WorkspaceReasonCommentResponseDto::from)
			.toList();
	}
}
