package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerDeleteWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkerScheduleUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/manager/workspace/{workspaceId}/worker-schedules")
public class ManagerWorkerScheduleController implements ManagerWorkerScheduleControllerSpec{

	private final ManagerCreateWorkerScheduleUseCase managerCreateWorkerScheduleUseCase;
	private final ManagerUpdateWorkerScheduleUseCase managerUpdateWorkerScheduleUseCase;
	private final ManagerDeleteWorkerScheduleUseCase managerDeleteWorkerScheduleUseCase;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createWorkerSchedule(
		@PathVariable Long workspaceId,
		@RequestBody @Valid CreateWorkerScheduleRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerCreateWorkerScheduleUseCase.execute(actor, workspaceId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@PatchMapping("/{workerScheduleId}")
	public ResponseEntity<CommonApiResponse<Void>> updateWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerScheduleId,
		@RequestBody @Valid UpdateWorkerScheduleRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerUpdateWorkerScheduleUseCase.execute(actor, workspaceId, workerScheduleId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@DeleteMapping("/{workerScheduleId}")
	public ResponseEntity<CommonApiResponse<Void>> deleteWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerScheduleId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerDeleteWorkerScheduleUseCase.execute(actor, workspaceId, workerScheduleId);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}
}
