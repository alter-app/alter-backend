package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceWorkerListUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<PaginatedResponseDto<ManagerWorkspaceWorkerListResponseDto>> getWorkspaceWorkerList(
        @PathVariable Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        PageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(managerGetWorkspaceWorkerList.execute(actor, workspaceId, filter, pageRequest));
    }

}
