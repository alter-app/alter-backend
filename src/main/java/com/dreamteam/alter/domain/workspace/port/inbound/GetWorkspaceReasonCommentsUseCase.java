package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface GetWorkspaceReasonCommentsUseCase {
	List<WorkspaceReasonCommentResponseDto> execute(User user, Long workspaceRequestId, Long reasonId);
}
