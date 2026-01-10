package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateWorkerScheduleUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/manager/workspaces/{workspaceId}")
public class ManagerWorkerScheduleController implements ManagerWorkerScheduleControllerSpec{

	@Resource(name = "managerCreateWorkerSchedule")
	private final ManagerCreateWorkerScheduleUseCase managerCreateWorkerScheduleUseCase;

	@Override
	@PostMapping("/workers/{workerId}/schedules")
	public ResponseEntity<CommonApiResponse<Void>> createWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerId,
		@RequestBody @Valid List<CreateWorkerScheduleRequestDto> request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerCreateWorkerScheduleUseCase.execute(actor, workspaceId, workerId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
