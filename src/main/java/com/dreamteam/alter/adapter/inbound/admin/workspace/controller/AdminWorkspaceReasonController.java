package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.CreateWorkspaceReasonDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests/{workspaceRequestId}/reasons")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceReasonController implements AdminWorkspaceReasonControllerSpec{

	private final CreateWorkspaceReasonUseCase createWorkspaceReason;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createReason(
		@PathVariable Long workspaceRequestId,
		@RequestBody CreateWorkspaceReasonDto request
	) {
		createWorkspaceReason.execute(workspaceRequestId, request.getReason());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
