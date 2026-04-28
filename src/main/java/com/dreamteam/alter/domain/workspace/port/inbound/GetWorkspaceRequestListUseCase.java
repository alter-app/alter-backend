package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;

public interface GetWorkspaceRequestListUseCase {
	List<WorkspaceRequestListResponseDto> execute(User user);
}
