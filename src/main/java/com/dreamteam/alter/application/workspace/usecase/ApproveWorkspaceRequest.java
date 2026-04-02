package com.dreamteam.alter.application.workspace.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.ApproveWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("approveWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class ApproveWorkspaceRequest implements ApproveWorkspaceRequestUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileDeleteService fileDeleteService;

	@Override
	public void execute(Long workspaceRequestId) {
		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findById(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 요청을 찾을 수 없습니다."));

		workspaceRequest.approve();

		Optional<File> file = fileQueryRepository.findByTargetTypeAndTargetId(
			FileTargetType.WORKSPACE_OWN_IDENTITY, String.valueOf(workspaceRequestId));

		file.ifPresent(fileDeleteService::delete);
	}
}
