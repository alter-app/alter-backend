package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetWorkspaceReasonListUseCase {

	List<WorkspaceReasonResponseDto> execute(AppActor actor, Long workspaceRequestId);
}
