package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceImagesRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceImagesUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkspaceImagesUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manager/workspaces")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceImageController implements ManagerWorkspaceImageControllerSpec {

    @Resource(name = "managerGetWorkspaceImages")
    private final ManagerGetWorkspaceImagesUseCase managerGetWorkspaceImages;

    @Resource(name = "managerUpdateWorkspaceImages")
    private final ManagerUpdateWorkspaceImagesUseCase managerUpdateWorkspaceImages;

    @Override
    @GetMapping("/{workspaceId}/images")
    public ResponseEntity<CommonApiResponse<List<WorkspaceImageResponseDto>>> getWorkspaceImages(
        @PathVariable Long workspaceId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetWorkspaceImages.execute(actor, workspaceId)));
    }

    @Override
    @PutMapping("/{workspaceId}/images")
    public ResponseEntity<CommonApiResponse<Void>> updateWorkspaceImages(
        @PathVariable Long workspaceId,
        @RequestBody @Valid UpdateWorkspaceImagesRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerUpdateWorkspaceImages.execute(actor, workspaceId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
