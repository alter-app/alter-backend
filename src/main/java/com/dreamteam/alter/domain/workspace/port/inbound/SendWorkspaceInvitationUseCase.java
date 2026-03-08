package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationResultDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface SendWorkspaceInvitationUseCase {
    SendWorkspaceInvitationResultDto execute(ManagerActor actor, Long workspaceId, SendWorkspaceInvitationRequestDto request);
}
