package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestCommentListUseCase;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspace-requests/{workspaceRequestId}/comments")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceRequestCommentController implements ManagerWorkspaceRequestCommentControllerSpec {

	@Resource(name = "createWorkspaceRequestComment")
	private final CreateWorkspaceRequestCommentUseCase createWorkspaceRequestComment;

	@Resource(name = "getWorkspaceRequestCommentList")
	private final GetWorkspaceRequestCommentListUseCase getWorkspaceRequestCommentList;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createComment(
		@PathVariable Long workspaceRequestId,
		@Valid @RequestBody CreateWorkspaceRequestCommentRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		createWorkspaceRequestComment.execute(actor.getManagerUser().getUser(), workspaceRequestId, CommentOwner.USER, request.getComment(), request.getFileIds());
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<List<WorkspaceRequestCommentResponseDto>>> getCommentList(
		@PathVariable Long workspaceRequestId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(
			getWorkspaceRequestCommentList.execute(workspaceRequestId, actor.getManagerUser().getUser(), CommentOwner.USER)));
	}
}
