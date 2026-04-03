package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;
import java.util.Optional;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.AdminWorkspaceRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestResponse;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;

public interface WorkspaceRequestQueryRepository {
	boolean existsByIdAndUserId(Long workspaceRequestId, Long userId);
	boolean existsById(Long workspaceRequestId);

	List<WorkspaceRequestListResponse> getWorkspaceRequestList(Long userId);

	WorkspaceRequestResponse getWorkspaceRequest(Long userId, Long workspaceRequestId);

	Optional<WorkspaceRequest> findByIdWithUser(Long workspaceRequestId);

	long countAll();

	List<AdminWorkspaceRequestListResponse> getAdminWorkspaceRequestListWithCursor(
		CursorPageRequest<CursorDto> pageRequest
	);
}
