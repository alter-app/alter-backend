package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceManagerListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateFixedScheduleDateRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceWorkerColorRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.command.UpdateWorkspaceWorkerColorCommand;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceManagerListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceWorkerListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateFixedScheduleDateUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkspaceWorkerColorCodeUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspaces")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceController implements ManagerWorkspaceControllerSpec {

    @Resource(name = "managerGetWorkspaceList")
    private final ManagerGetWorkspaceListUseCase managerGetWorkspaceList;

    @Resource(name = "managerGetWorkspace")
    private final ManagerGetWorkspaceUseCase managerGetWorkspace;

    @Resource(name = "managerGetWorkspaceWorkerList")
    private final ManagerGetWorkspaceWorkerListUseCase managerGetWorkspaceWorkerList;

    @Resource(name = "managerGetWorkspaceManagerList")
    private final ManagerGetWorkspaceManagerListUseCase managerGetWorkspaceManagerList;

    @Resource(name = "managerUpdateFixedScheduleDate")
    private final ManagerUpdateFixedScheduleDateUseCase managerUpdateFixedScheduleDate;

    @Resource(name = "managerUpdateWorkspaceWorkerColorCode")
    private final ManagerUpdateWorkspaceWorkerColorCodeUseCase managerUpdateWorkspaceWorkerColor;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<ManagerWorkspaceListResponseDto>>> getWorkspaceList() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetWorkspaceList.execute(actor)));
    }

    @Override
    @GetMapping("/{workspaceId}")
    public ResponseEntity<CommonApiResponse<ManagerWorkspaceResponseDto>> getWorkspaceDetail(
        @PathVariable Long workspaceId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetWorkspace.execute(actor, workspaceId)));
    }

    @Override
    @GetMapping("/{workspaceId}/workers")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerWorkspaceWorkerListResponseDto>>> getWorkspaceWorkerList(
        @PathVariable Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetWorkspaceWorkerList.execute(actor, workspaceId, filter, pageRequest)));
    }

    @Override
    @GetMapping("/{workspaceId}/managers")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerWorkspaceManagerListResponseDto>>> getWorkspaceManagerList(
        @PathVariable Long workspaceId,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetWorkspaceManagerList.execute(actor, workspaceId, pageRequest)));
    }

    @Override
    @PatchMapping("/{workspaceId}/fixed-schedule")
    public ResponseEntity<CommonApiResponse<Void>> updateFixedScheduleDate(
        @PathVariable Long workspaceId,
        @RequestBody @Valid UpdateFixedScheduleDateRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerUpdateFixedScheduleDate.execute(actor, workspaceId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PatchMapping("/{workspaceId}/workers/{workerId}/color")
    public ResponseEntity<CommonApiResponse<Void>> updateWorkspaceWorkerColorCode(
        @PathVariable Long workspaceId,
        @PathVariable Long workerId,
        @RequestBody @Valid UpdateWorkspaceWorkerColorRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerUpdateWorkspaceWorkerColor.execute(
            actor,
            workspaceId,
            workerId,
            UpdateWorkspaceWorkerColorCommand.of(request.getColorCode())
        );
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
