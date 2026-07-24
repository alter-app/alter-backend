package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

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

import com.dreamteam.alter.adapter.inbound.common.dto.BusinessTypeResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CancelWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetBusinessTypeListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/app/workspace-requests")
@RequiredArgsConstructor
@Validated
public class UserWorkspaceRequestController implements UserWorkspaceRequestControllerSpec {

    @Resource(name = "createWorkspaceRequest")
    private final CreateWorkspaceRequestUseCase createWorkspaceRequest;

    @Resource(name = "getWorkspaceRequestList")
    private final GetWorkspaceRequestListUseCase getWorkspaceRequestList;

    @Resource(name = "getWorkspaceRequest")
    private final GetWorkspaceRequestUseCase getWorkspaceRequest;

    @Resource(name = "cancelWorkspaceRequest")
    private final CancelWorkspaceRequestUseCase cancelWorkspaceRequest;

    @Resource(name = "getBusinessTypeList")
    private final GetBusinessTypeListUseCase getBusinessTypeList;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createWorkspaceRequest(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createWorkspaceRequest.execute(actor.getUser(), request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<WorkspaceRequestListResponseDto>>> getWorkspaceRequestList() {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceRequestList.execute(actor.getUser())));
    }

    @Override
    @GetMapping("/business-types")
    public ResponseEntity<CommonApiResponse<List<BusinessTypeResponseDto>>> getBusinessTypeList() {
        List<BusinessTypeResponseDto> businessTypes = getBusinessTypeList.execute().stream()
            .map(BusinessTypeResponseDto::from)
            .toList();
        return ResponseEntity.ok(CommonApiResponse.of(businessTypes));
    }

    @Override
    @GetMapping("/{workspaceRequestId}")
    public ResponseEntity<CommonApiResponse<WorkspaceRequestResponseDto>> getWorkspaceRequestDetail(
        @PathVariable Long workspaceRequestId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceRequest.execute(actor.getUser(), workspaceRequestId)));
    }

    @Override
    @PatchMapping("/{workspaceRequestId}/cancel")
    public ResponseEntity<CommonApiResponse<Void>> cancelWorkspaceRequest(
        @PathVariable Long workspaceRequestId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        cancelWorkspaceRequest.execute(actor.getUser(), workspaceRequestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
