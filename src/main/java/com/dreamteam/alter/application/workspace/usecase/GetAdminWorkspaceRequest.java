package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceRequest")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceRequest implements GetAdminWorkspaceRequestUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

	@Override
	public AdminWorkspaceRequestResponseDto execute(Long workspaceRequestId) {
		WorkspaceRequestResponse workspaceRequest =
			workspaceRequestQueryRepository.getWorkspaceRequest(workspaceRequestId);

		if (ObjectUtils.isEmpty(workspaceRequest)) {
			throw new CustomException(ErrorCode.NOT_FOUND, "업장 등록 신청을 찾을 수 없습니다.");
		}

		return AdminWorkspaceRequestResponseDto.from(workspaceRequest);
	}
}
