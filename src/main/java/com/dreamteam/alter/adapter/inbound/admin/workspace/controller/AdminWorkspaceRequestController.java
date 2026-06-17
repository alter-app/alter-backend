package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.UpdateWorkspaceRequestStatusDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.UpdateWorkspaceRequestStatusUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceRequestController implements AdminWorkspaceRequestControllerSpec {

	@Resource(name = "getAdminWorkspaceRequestList")
	private final GetAdminWorkspaceRequestListUseCase getAdminWorkspaceRequestList;

	@Resource(name = "getAdminWorkspaceRequest")
	private final GetAdminWorkspaceRequestUseCase getAdminWorkspaceRequest;

	@Resource(name = "updateWorkspaceRequestStatus")
	private final UpdateWorkspaceRequestStatusUseCase updateWorkspaceRequestStatus;

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<PaginatedResponseDto<AdminWorkspaceRequestListResponseDto>>> getWorkspaceRequestList(
		PageRequestDto request
	) {
		return ResponseEntity.ok(CommonApiResponse.of(getAdminWorkspaceRequestList.execute(request)));
	}

	@Override
	@GetMapping("/{workspaceRequestId}")
	public ResponseEntity<CommonApiResponse<AdminWorkspaceRequestResponseDto>> getWorkspaceRequest(
		@PathVariable Long workspaceRequestId
	) {
		return ResponseEntity.ok(CommonApiResponse.of(getAdminWorkspaceRequest.execute(workspaceRequestId)));
	}

	@Override
	@PatchMapping("/{workspaceRequestId}/status")
	public ResponseEntity<CommonApiResponse<Void>> updateStatus(
		@PathVariable Long workspaceRequestId,
		@RequestBody @Valid UpdateWorkspaceRequestStatusDto request
	) {
		updateWorkspaceRequestStatus.execute(workspaceRequestId, request.getStatus());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
