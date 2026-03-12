package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface AcceptWorkspaceInvitationUseCase {
    void execute(AppActor actor, Long invitationId);
}
