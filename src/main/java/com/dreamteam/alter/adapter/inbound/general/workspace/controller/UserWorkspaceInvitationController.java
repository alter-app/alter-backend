package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.*;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/app")
public class UserWorkspaceInvitationController implements UserWorkspaceInvitationControllerSpec {

    @Resource(name = "sendJoinRequest")
    private final SendJoinRequestUseCase sendJoinRequestUseCase;

    @Resource(name = "getMyJoinRequestList")
    private final GetMyJoinRequestListUseCase getMyJoinRequestListUseCase;

    @Resource(name = "getMyInvitationList")
    private final GetMyInvitationListUseCase getMyInvitationListUseCase;

    @Resource(name = "acceptWorkspaceInvitation")
    private final AcceptWorkspaceInvitationUseCase acceptWorkspaceInvitationUseCase;

    @Resource(name = "declineWorkspaceInvitation")
    private final DeclineWorkspaceInvitationUseCase declineWorkspaceInvitationUseCase;

    @Override
    @PostMapping("/workspaces/{workspaceId}/join-requests")
    public ResponseEntity<CommonApiResponse<Void>> sendJoinRequest(
        @PathVariable Long workspaceId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        sendJoinRequestUseCase.execute(actor, workspaceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/users/me/join-requests")
    public ResponseEntity<CursorPaginatedApiResponse<MyJoinRequestResponseDto>> getMyJoinRequestList(
        CursorPageRequestDto cursorPageRequest,
        @RequestParam(required = false) BusinessJoinRequestStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyJoinRequestListUseCase.execute(actor, status, from, to, cursorPageRequest));
    }

    @Override
    @GetMapping("/users/me/invitations")
    public ResponseEntity<CursorPaginatedApiResponse<MyInvitationResponseDto>> getMyInvitationList(
        CursorPageRequestDto cursorPageRequest,
        @RequestParam(required = false) BusinessInvitationStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyInvitationListUseCase.execute(actor, status, from, to, cursorPageRequest));
    }

    @Override
    @PostMapping("/users/me/invitations/{invitationId}/accept")
    public ResponseEntity<CommonApiResponse<Void>> acceptInvitation(
        @PathVariable Long invitationId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        acceptWorkspaceInvitationUseCase.execute(actor, invitationId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/users/me/invitations/{invitationId}/decline")
    public ResponseEntity<CommonApiResponse<Void>> declineInvitation(
        @PathVariable Long invitationId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        declineWorkspaceInvitationUseCase.execute(actor, invitationId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
