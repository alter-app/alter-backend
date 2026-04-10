package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceReason")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceReason implements CreateWorkspaceReasonUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceReasonRepository workspaceReasonRepository;

	@Override
	public void execute(Long workspaceRequestId, String reason) {
		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 등록 신청 요청을 찾을 수 없습니다."));

		workspaceReasonRepository.save(WorkspaceReason.create(workspaceRequest, reason));
		workspaceRequest.reject();
	}
}
