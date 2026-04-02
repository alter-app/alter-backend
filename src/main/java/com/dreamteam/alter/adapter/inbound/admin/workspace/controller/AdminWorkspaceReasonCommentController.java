package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminCreateWorkspaceReasonCommentUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests/{workspaceRequestId}/reasons/{reasonId}/comments")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceReasonCommentController implements AdminWorkspaceReasonCommentControllerSpec{

	@Resource(name = "adminCreateWorkspaceReasonComment")
	private final AdminCreateWorkspaceReasonCommentUseCase adminCreateWorkspaceReasonComment;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createComment(
		@PathVariable Long workspaceRequestId,
		@PathVariable Long reasonId,
		@RequestBody @Valid CreateWorkspaceReasonCommentRequestDto request
	) {
		adminCreateWorkspaceReasonComment.execute(workspaceRequestId, reasonId, request.getComment());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
