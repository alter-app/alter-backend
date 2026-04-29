package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface GetWorkspaceReasonListUseCase {

	List<WorkspaceReasonResponseDto> execute(User user, Long workspaceRequestId);
}
