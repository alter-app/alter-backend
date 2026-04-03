package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.workspace.port.inbound.ApproveWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestListUseCase;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceRequestController implements AdminWorkspaceRequestControllerSpec {

	@Resource(name = "getAdminWorkspaceRequestList")
	private final GetAdminWorkspaceRequestListUseCase getAdminWorkspaceRequestList;

	@Resource(name = "approveWorkspaceRequest")
	private final ApproveWorkspaceRequestUseCase approveWorkspaceRequest;

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<AdminWorkspaceRequestListResponseDto>>> getWorkspaceRequestList(
		CursorPageRequestDto request
	) {
		return ResponseEntity.ok(CommonApiResponse.of(getAdminWorkspaceRequestList.execute(request)));
	}

	@Override
	@PostMapping("/{workspaceRequestId}/approve")
	public ResponseEntity<CommonApiResponse<Void>> approve(@PathVariable Long workspaceRequestId) {
		approveWorkspaceRequest.execute(workspaceRequestId);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
