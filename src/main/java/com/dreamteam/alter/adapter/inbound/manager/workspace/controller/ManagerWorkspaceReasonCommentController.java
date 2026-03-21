package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonCommentUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonCommentsUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspaces/{workspaceId}/reasons/{reasonId}/comments")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceReasonCommentController implements ManagerWorkspaceReasonCommentControllerSpec {

	@Resource(name = "createWorkspaceReasonComment")
	private final CreateWorkspaceReasonCommentUseCase createWorkspaceReasonComment;

	@Resource(name = "getWorkspaceReasonComments")
	private final GetWorkspaceReasonCommentsUseCase getWorkspaceReasonComments;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createWorkspaceReasonComment(
		@PathVariable Long workspaceId,
		@PathVariable Long reasonId,
		@Valid @RequestBody CreateWorkspaceReasonCommentRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		createWorkspaceReasonComment.execute(actor, workspaceId, reasonId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<List<WorkspaceReasonCommentResponseDto>>> getWorkspaceReasonComments(
		@PathVariable Long workspaceId,
		@PathVariable Long reasonId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceReasonComments.execute(actor, workspaceId, reasonId)));
	}

}
