package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Validated
public class UserWorkspaceController implements UserWorkspaceControllerSpec {

    @Resource(name = "createWorkspace")
    private final CreateWorkspaceUseCase createWorkspace;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<CommonApiResponse<Void>> createWorkspace(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createWorkspace.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
