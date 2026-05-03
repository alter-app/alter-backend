package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.CreateWorkspaceReasonDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminGetWorkspaceReasonListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests/{workspaceRequestId}/reasons")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceReasonController implements AdminWorkspaceReasonControllerSpec{

	@Resource(name = "adminGetWorkspaceReasonList")
	private final AdminGetWorkspaceReasonListUseCase adminGetWorkspaceReasonList;

	@Resource(name = "createWorkspaceReason")
	private final CreateWorkspaceReasonUseCase createWorkspaceReason;

	@GetMapping
	public ResponseEntity<CommonApiResponse<List<WorkspaceReasonResponseDto>>> getWorkspaceReasonList(
		@PathVariable Long workspaceRequestId
	) {
		return ResponseEntity.ok(CommonApiResponse.of(adminGetWorkspaceReasonList.execute(workspaceRequestId)));
	}

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createReason(
		@PathVariable Long workspaceRequestId,
		@RequestBody @Valid CreateWorkspaceReasonDto request
	) {
		createWorkspaceReason.execute(workspaceRequestId, request.getReason());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
