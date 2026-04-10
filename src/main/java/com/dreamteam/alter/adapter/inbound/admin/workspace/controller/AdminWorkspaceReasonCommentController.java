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

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminCreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminCreateWorkspaceReasonCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceReasonCommentListUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests/{workspaceRequestId}/reasons/{reasonId}/comments")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceReasonCommentController implements AdminWorkspaceReasonCommentControllerSpec {

	@Resource(name = "getAdminWorkspaceReasonCommentList")
	private final GetAdminWorkspaceReasonCommentListUseCase getAdminWorkspaceReasonCommentList;

	@Resource(name = "adminCreateWorkspaceReasonComment")
	private final AdminCreateWorkspaceReasonCommentUseCase adminCreateWorkspaceReasonComment;

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<List<AdminWorkspaceReasonCommentResponseDto>>> getCommentList(
		@PathVariable Long workspaceRequestId,
		@PathVariable Long reasonId
	) {
		return ResponseEntity.ok(CommonApiResponse.of(getAdminWorkspaceReasonCommentList.execute(workspaceRequestId, reasonId)));
	}

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createComment(
		@PathVariable Long workspaceRequestId,
		@PathVariable Long reasonId,
		@RequestBody @Valid AdminCreateWorkspaceReasonCommentRequestDto request
	) {
		AdminActor actor = AdminActionContext.getInstance().getActor();
		adminCreateWorkspaceReasonComment.execute(actor, workspaceRequestId, reasonId, request.getComment());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
