package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceRequest")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceRequest implements GetWorkspaceRequestUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileUrlService fileUrlService;

	@Override
	public WorkspaceRequestResponseDto execute(User user, Long workspaceRequestId) {
		WorkspaceRequestResponse workspaceRequest = workspaceRequestQueryRepository.getWorkspaceRequest(user.getId(), workspaceRequestId);

		if (ObjectUtils.isEmpty(workspaceRequest)) {
			throw new CustomException(ErrorCode.NOT_FOUND, "등록 신청한 업장을 찾을 수 없습니다.");
		}

		String targetId = workspaceRequestId.toString();
		return WorkspaceRequestResponseDto.of(
			workspaceRequest,
			resolvePresignedUrl(FileTargetType.WORKSPACE_CERTIFICATE, targetId),
			resolvePresignedUrl(FileTargetType.WORKSPACE_OWN_IDENTITY, targetId),
			resolvePresignedUrl(FileTargetType.WORKSPACE_WARRANT, targetId)
		);
	}

	private String resolvePresignedUrl(FileTargetType targetType, String targetId) {
		return fileQueryRepository.findByTargetTypeAndTargetId(targetType, targetId)
			.map(file -> fileUrlService.resolve(file).getUrl())
			.orElse(null);
	}
}
