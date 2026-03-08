package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationResultDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.*;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/workspaces")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerWorkspaceInvitationController implements ManagerWorkspaceInvitationControllerSpec {

    @Resource(name = "sendWorkspaceInvitation")
    private final SendWorkspaceInvitationUseCase sendWorkspaceInvitationUseCase;

    @Resource(name = "getWorkspaceJoinRequestList")
    private final GetWorkspaceJoinRequestListUseCase getWorkspaceJoinRequestListUseCase;

    @Resource(name = "approveJoinRequest")
    private final ApproveJoinRequestUseCase approveJoinRequestUseCase;

    @Resource(name = "rejectJoinRequest")
    private final RejectJoinRequestUseCase rejectJoinRequestUseCase;

    @Override
    @PostMapping("/{workspaceId}/invitations")
    public ResponseEntity<CommonApiResponse<SendWorkspaceInvitationResultDto>> sendInvitation(
        @PathVariable Long workspaceId,
        @Valid @RequestBody SendWorkspaceInvitationRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        SendWorkspaceInvitationResultDto result = sendWorkspaceInvitationUseCase.execute(actor, workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.of(result));
    }

    @Override
    @GetMapping("/{workspaceId}/join-requests")
    public ResponseEntity<CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto>> getJoinRequestList(
        @PathVariable Long workspaceId,
        CursorPageRequestDto cursorPageRequest,
        @RequestParam(required = false) BusinessJoinRequestStatus status
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(getWorkspaceJoinRequestListUseCase.execute(actor, workspaceId, status, cursorPageRequest));
    }

    @Override
    @PostMapping("/{workspaceId}/join-requests/{requestId}/approve")
    public ResponseEntity<CommonApiResponse<Void>> approveJoinRequest(
        @PathVariable Long workspaceId,
        @PathVariable Long requestId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        approveJoinRequestUseCase.execute(actor, workspaceId, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/{workspaceId}/join-requests/{requestId}/reject")
    public ResponseEntity<CommonApiResponse<Void>> rejectJoinRequest(
        @PathVariable Long workspaceId,
        @PathVariable Long requestId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        rejectJoinRequestUseCase.execute(actor, workspaceId, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
