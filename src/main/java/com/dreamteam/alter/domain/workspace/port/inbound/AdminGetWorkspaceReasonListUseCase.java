package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;

public interface AdminGetWorkspaceReasonListUseCase {
	List<WorkspaceReasonResponseDto> execute(Long workspaceRequestId);
}
