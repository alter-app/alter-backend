package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceReasonCommentUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspaces/{workspaceId}/reasons/{reasonId}/comment")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceReasonCommentController implements ManagerWorkspaceReasonCommentControllerSpec {

	@Resource(name = "createWorkspaceReasonComment")
	private final CreateWorkspaceReasonCommentUseCase createWorkspaceReasonComment;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createWorkspaceReason(
		@PathVariable Long workspaceId,
		@PathVariable Long reasonId,
		@Valid @RequestBody CreateWorkspaceReasonCommentRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		createWorkspaceReasonComment.execute(actor, workspaceId, reasonId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
