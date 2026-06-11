package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

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

	private static final List<FileTargetType> ATTACHMENT_TARGET_TYPES = List.of(
		FileTargetType.WORKSPACE_CERTIFICATE,
		FileTargetType.WORKSPACE_OWN_IDENTITY,
		FileTargetType.WORKSPACE_WARRANT
	);

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileDeleteService fileDeleteService;

	@Override
	public void execute(User user, Long workspaceRequestId) {
		if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, user.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 요청을 찾을 수 없습니다."));

		workspaceRequest.cancel();

		// 개인정보 파기: 증명원 · 신분증 · 위임장 첨부 파일 삭제
		String targetId = String.valueOf(workspaceRequestId);
		for (FileTargetType targetType : ATTACHMENT_TARGET_TYPES) {
			fileQueryRepository.findByTargetTypeAndTargetId(targetType, targetId)
				.ifPresent(fileDeleteService::delete);
		}
	}
}
