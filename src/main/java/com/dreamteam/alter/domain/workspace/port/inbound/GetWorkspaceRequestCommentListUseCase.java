package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;

public interface GetWorkspaceRequestCommentListUseCase {
	List<WorkspaceRequestCommentResponseDto> execute(Long workspaceRequestId);
}
