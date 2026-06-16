package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.CancelWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("cancelWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class CancelWorkspaceRequest implements CancelWorkspaceRequestUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileDeleteService fileDeleteService;

	@Override
	public void execute(User user, Long workspaceRequestId) {
		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "등록 신청한 업장을 찾을 수 없습니다."));

		if (!workspaceRequest.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.NOT_FOUND, "등록 신청한 업장을 찾을 수 없습니다.");
		}

		workspaceRequest.cancel();

		fileQueryRepository.findByTargetTypeAndTargetId(
				FileTargetType.WORKSPACE_OWN_IDENTITY, String.valueOf(workspaceRequestId))
			.ifPresent(fileDeleteService::delete);
	}
}
