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

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestCommentListUseCase;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/workspace-requests/{workspaceRequestId}/comments")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWorkspaceRequestCommentController implements AdminWorkspaceRequestCommentControllerSpec {

	@Resource(name = "getWorkspaceRequestCommentList")
	private final GetWorkspaceRequestCommentListUseCase getWorkspaceRequestCommentList;

	@Resource(name = "createWorkspaceRequestComment")
	private final CreateWorkspaceRequestCommentUseCase createWorkspaceRequestComment;

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<List<WorkspaceRequestCommentResponseDto>>> getCommentList(
		@PathVariable Long workspaceRequestId
	) {
		AdminActor actor = AdminActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(
			getWorkspaceRequestCommentList.execute(workspaceRequestId, actor.getUser(), CommentOwner.ADMIN)));
	}

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createComment(
		@PathVariable Long workspaceRequestId,
		@RequestBody @Valid CreateWorkspaceRequestCommentRequestDto request
	) {
		AdminActor actor = AdminActionContext.getInstance().getActor();
		createWorkspaceRequestComment.execute(actor.getUser(), workspaceRequestId, CommentOwner.ADMIN, request.getComment(), request.getFileIds());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
