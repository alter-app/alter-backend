package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateWorkspaceReasonCommentUseCase {
    void execute(AppActor actor, Long workspaceRequestId, Long reasonId, CreateWorkspaceReasonCommentRequestDto request);
}
