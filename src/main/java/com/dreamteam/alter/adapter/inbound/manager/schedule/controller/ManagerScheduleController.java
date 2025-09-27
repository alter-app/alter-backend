package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.AssignWorkerRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.CreateWorkScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerWorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/manager/schedules")
public class ManagerScheduleController implements ManagerScheduleControllerSpec {

    @Resource(name = "managerCreateWorkSchedule")
    private final ManagerCreateScheduleUseCase managerCreateSchedule;

    @Resource(name = "managerGetWorkScheduleList")
    private final ManagerGetScheduleListUseCase managerGetScheduleList;

    @Resource(name = "managerUpdateWorkSchedule")
    private final ManagerUpdateScheduleUseCase managerUpdateSchedule;

    @Resource(name = "managerDeleteWorkSchedule")
    private final ManagerDeleteScheduleUseCase managerDeleteSchedule;

    @Resource(name = "managerAssignWorkerToSchedule")
    private final ManagerAssignWorkerUseCase managerAssignWorker;

    @Resource(name = "managerUpdateWorkerInSchedule")
    private final ManagerUpdateWorkerUseCase managerUpdateWorker;

    @Resource(name = "managerRemoveWorkerFromSchedule")
    private final ManagerRemoveWorkerUseCase managerRemoveWorker;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createSchedule(
        @Valid @RequestBody CreateWorkScheduleRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerCreateSchedule.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<ManagerScheduleResponseDto>>> getScheduleList(
        ManagerWorkScheduleInquiryRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetScheduleList.execute(actor, request.getWorkspaceId(), request.getYear(), request.getMonth())));
    }

    @Override
    @PutMapping("/{workShiftId}")
    public ResponseEntity<CommonApiResponse<Void>> updateSchedule(
        @PathVariable Long workShiftId,
        @Valid @RequestBody UpdateWorkScheduleRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerUpdateSchedule.execute(actor, workShiftId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/{workShiftId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteSchedule(
        @PathVariable Long workShiftId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerDeleteSchedule.execute(actor, workShiftId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/{workShiftId}/workers")
    public ResponseEntity<CommonApiResponse<Void>> assignWorker(
        @PathVariable Long workShiftId,
        @Valid @RequestBody AssignWorkerRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerAssignWorker.execute(actor, workShiftId, request.getWorkerId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/{workShiftId}/workers")
    public ResponseEntity<CommonApiResponse<Void>> updateWorker(
        @PathVariable Long workShiftId,
        @Valid @RequestBody UpdateWorkerRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerUpdateWorker.execute(actor, workShiftId, request.getWorkerId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/{workShiftId}/workers")
    public ResponseEntity<CommonApiResponse<Void>> removeWorker(
        @PathVariable Long workShiftId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerRemoveWorker.execute(actor, workShiftId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
