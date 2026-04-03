package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonListUseCase;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/app/workspace-requests/{workspaceRequestId}/reasons")
@RequiredArgsConstructor
@Validated
public class UserWorkspaceReasonController {

	@Resource(name = "getWorkspaceReasons")
	private final GetWorkspaceReasonListUseCase getWorkspaceReasons;

	@GetMapping
	public ResponseEntity<CommonApiResponse<List<WorkspaceReasonResponseDto>>> getWorkspaceReasonList(
		@PathVariable Long workspaceRequestId
	) {
		AppActor actor = AppActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceReasons.execute(actor, workspaceRequestId)));
	}
}
