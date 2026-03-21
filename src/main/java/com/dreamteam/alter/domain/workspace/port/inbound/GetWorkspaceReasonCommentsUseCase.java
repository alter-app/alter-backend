package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface GetWorkspaceReasonCommentsUseCase {
	List<WorkspaceReasonCommentResponseDto> execute(ManagerActor actor, Long workspaceId, Long reasonId);
}
