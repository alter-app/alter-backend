package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminGetWorkspaceReasonListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service( "adminGetWorkspaceReasonList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetWorkspaceReasonList implements AdminGetWorkspaceReasonListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;

	@Override
	public List<WorkspaceReasonResponseDto> execute(Long workspaceRequestId) {
		if (!workspaceRequestQueryRepository.existsById(workspaceRequestId)) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}

		return workspaceReasonQueryRepository.getWorkspaceReasonList(workspaceRequestId)
			.stream()
			.map(WorkspaceReasonResponseDto::from)
			.toList();
	}
}
