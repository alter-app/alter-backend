package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.FixedWorkerScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerCreateFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerDeleteFixedWorkerScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetFixedWorkerScheduleListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateFixedWorkerScheduleUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/manager/workspaces/{workspaceId}/fixed-worker-schedules")
public class ManagerFixedWorkerScheduleController implements ManagerFixedWorkerScheduleControllerSpec {

	@Resource(name = "managerCreateFixedWorkerSchedule")
	private final ManagerCreateFixedWorkerScheduleUseCase managerCreateFixedWorkerSchedule;

	@Resource(name = "managerUpdateWorkerSchedule")
	private final ManagerUpdateFixedWorkerScheduleUseCase managerUpdateWorkerSchedule;

	@Resource(name = "managerDeleteFixedWorkerSchedule")
	private final ManagerDeleteFixedWorkerScheduleUseCase managerDeleteFixedWorkerSchedule;

	@Resource(name = "managerGetFixedWorkerScheduleList")
	private final ManagerGetFixedWorkerScheduleListUseCase managerGetFixedWorkerScheduleList;

	@Override
	@PostMapping
	public ResponseEntity<CommonApiResponse<Void>> createWorkerSchedule(
		@PathVariable Long workspaceId,
		@RequestBody @Valid CreateWorkerScheduleRequestDto request
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerCreateFixedWorkerSchedule.execute(actor, workspaceId, request);
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
		managerUpdateWorkerSchedule.execute(actor, workspaceId, workerScheduleId, request);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@DeleteMapping("/{workerScheduleId}")
	public ResponseEntity<CommonApiResponse<Void>> deleteWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerScheduleId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		managerDeleteFixedWorkerSchedule.execute(actor, workspaceId, workerScheduleId);
		return ResponseEntity.ok(CommonApiResponse.empty());
	}

	@Override
	@GetMapping
	public ResponseEntity<CommonApiResponse<List<FixedWorkerScheduleResponseDto>>> getWorkerScheduleList(
		@PathVariable Long workspaceId
	) {
		ManagerActor actor = ManagerActionContext.getInstance().getActor();
		return ResponseEntity.ok(CommonApiResponse.of(managerGetFixedWorkerScheduleList.execute(actor, workspaceId)));
	}
}
