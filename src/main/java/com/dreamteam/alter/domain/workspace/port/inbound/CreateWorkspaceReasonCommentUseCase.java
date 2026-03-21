package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface CreateWorkspaceReasonCommentUseCase {
    void execute(ManagerActor actor, Long workspaceId, Long reasonId, CreateWorkspaceReasonCommentRequestDto request);
}
