package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonCommentsUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceReasonComments")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceReasonComments implements GetWorkspaceReasonCommentsUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
	private final WorkspaceReasonCommentQueryRepository workspaceReasonCommentQueryRepository;

	@Override
	public List<WorkspaceReasonCommentResponseDto> execute(AppActor actor, Long workspaceRequestId, Long reasonId) {
		if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, actor.getUserId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		workspaceReasonQueryRepository.findByIdAndWorkspaceRequestId(reasonId, workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		return workspaceReasonCommentQueryRepository.getWorkspaceReasonCommentList(reasonId)
			.stream()
			.map(WorkspaceReasonCommentResponseDto::from)
			.toList();
	}
}
