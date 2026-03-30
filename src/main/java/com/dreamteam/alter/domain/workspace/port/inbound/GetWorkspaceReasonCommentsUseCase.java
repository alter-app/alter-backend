package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface GetWorkspaceReasonCommentsUseCase {
	List<WorkspaceReasonCommentResponseDto> execute(AppActor actor, Long workspaceRequestId, Long reasonId);
}
