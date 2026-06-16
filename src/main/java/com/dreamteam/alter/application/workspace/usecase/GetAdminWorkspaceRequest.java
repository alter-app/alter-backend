package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceRequest")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceRequest implements GetAdminWorkspaceRequestUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileUrlService fileUrlService;

	@Override
	public AdminWorkspaceRequestResponseDto execute(Long workspaceRequestId) {
		WorkspaceRequestResponse workspaceRequest =
			workspaceRequestQueryRepository.getWorkspaceRequest(workspaceRequestId);

		if (ObjectUtils.isEmpty(workspaceRequest)) {
			throw new CustomException(ErrorCode.NOT_FOUND, "업장 등록 신청을 찾을 수 없습니다.");
		}

		String targetId = workspaceRequestId.toString();
		return AdminWorkspaceRequestResponseDto.from(
			workspaceRequest,
			fileUrlService.resolveUrlByTarget(FileTargetType.WORKSPACE_CERTIFICATE, targetId),
			fileUrlService.resolveUrlByTarget(FileTargetType.WORKSPACE_OWN_IDENTITY, targetId),
			fileUrlService.resolveUrlByTarget(FileTargetType.WORKSPACE_WARRANT, targetId)
		);
	}
}
