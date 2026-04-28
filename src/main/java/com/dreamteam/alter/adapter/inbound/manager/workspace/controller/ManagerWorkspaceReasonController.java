package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

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
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonListUseCase;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspace-requests/{workspaceRequestId}/reasons")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceReasonController implements ManagerWorkspaceReasonControllerSpec {

	@Resource(name = "getWorkspaceReasonList")
	private final GetWorkspaceReasonListUseCase getWorkspaceReasonList;

	@GetMapping
	@Override
	public ResponseEntity<CommonApiResponse<List<WorkspaceReasonResponseDto>>> getWorkspaceReasonList(
		@PathVariable Long workspaceRequestId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceReasonList.execute(actor.getManagerUser().getUser(), workspaceRequestId)));
	}
}
