package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceReasonCommentListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceReasonCommentList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceReasonCommentList implements GetAdminWorkspaceReasonCommentListUseCase {

	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
	private final WorkspaceReasonCommentQueryRepository workspaceReasonCommentQueryRepository;

	@Override
	public List<AdminWorkspaceReasonCommentResponseDto> execute(Long workspaceRequestId, Long reasonId) {
		workspaceReasonQueryRepository.findByIdAndWorkspaceRequestId(reasonId, workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		return workspaceReasonCommentQueryRepository.getWorkspaceReasonCommentList(reasonId)
			.stream()
			.map(AdminWorkspaceReasonCommentResponseDto::from)
			.toList();
	}
}
