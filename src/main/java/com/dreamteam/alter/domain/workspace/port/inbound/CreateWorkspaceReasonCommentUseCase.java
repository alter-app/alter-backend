package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface CreateWorkspaceReasonCommentUseCase {
    void execute(User user, Long workspaceRequestId, Long reasonId, CreateWorkspaceReasonCommentRequestDto request);
}
