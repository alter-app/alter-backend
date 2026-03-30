package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestListUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/app/workspaces")
@RequiredArgsConstructor
@Validated
public class UserWorkspaceRequestRequestController implements UserWorkspaceRequestControllerSpec {

    @Resource(name = "createWorkspaceRequest")
    private final CreateWorkspaceRequestUseCase createWorkspaceRequest;

    @Resource(name = "getWorkspaceRequestList")
    private final GetWorkspaceRequestListUseCase getWorkspaceRequestList;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createWorkspace(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createWorkspaceRequest.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @GetMapping
    public ResponseEntity<CommonApiResponse<List<WorkspaceRequestListResponseDto>>> getWorkspaces() {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getWorkspaceRequestList.execute(actor)));
    }
}
