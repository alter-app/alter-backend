package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceReasonCommentResponseDto;

public interface GetAdminWorkspaceReasonCommentListUseCase {

	List<AdminWorkspaceReasonCommentResponseDto> execute(Long workspaceRequestId, Long reasonId);
}
