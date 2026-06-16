package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CancelWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspace-requests")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceRequestController implements ManagerWorkspaceRequestControllerSpec {

    @Resource(name = "createWorkspaceRequest")
    private final CreateWorkspaceRequestUseCase createWorkspaceRequest;

    @Resource(name = "getWorkspaceRequestList")
    private final GetWorkspaceRequestListUseCase getWorkspaceRequestList;

    @Resource(name = "getWorkspaceRequest")
    private final GetWorkspaceRequestUseCase getWorkspaceRequest;

    @Resource(name = "cancelWorkspaceRequest")
    private final CancelWorkspaceRequestUseCase cancelWorkspaceRequest;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createWorkspaceRequest(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        createWorkspaceRequest.execute(actor.getManagerUser().getUser(), request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<WorkspaceRequestListResponseDto>>> getWorkspaceRequestList() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceRequestList.execute(actor.getManagerUser().getUser())));
    }

    @Override
    @GetMapping("/{workspaceRequestId}")
    public ResponseEntity<CommonApiResponse<WorkspaceRequestResponseDto>> getWorkspaceRequestDetail(
        @PathVariable Long workspaceRequestId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceRequest.execute(actor.getManagerUser().getUser(), workspaceRequestId)));
    }

    @Override
    @PatchMapping("/{workspaceRequestId}/cancel")
    public ResponseEntity<CommonApiResponse<Void>> cancelWorkspaceRequest(
        @PathVariable Long workspaceRequestId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        cancelWorkspaceRequest.execute(actor.getManagerUser().getUser(), workspaceRequestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
