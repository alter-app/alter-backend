package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceReasonList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceReasonList implements GetWorkspaceReasonListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;

	@Override
	public List<WorkspaceReasonResponseDto> execute(User user, Long workspaceRequestId) {
		if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, user.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		return workspaceReasonQueryRepository.getWorkspaceReasonList(workspaceRequestId)
			.stream()
			.map(WorkspaceReasonResponseDto::from)
			.toList();
	}
}
