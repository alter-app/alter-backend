package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.*;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.*;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/app")
public class UserSubstituteRequestController implements UserSubstituteRequestControllerSpec {

    @Resource(name = "getExchangeableWorkers")
    private final GetExchangeableWorkersUseCase getExchangeableWorkersUseCase;

    @Resource(name = "createSubstituteRequest")
    private final CreateSubstituteRequestUseCase createSubstituteRequestUseCase;

    @Resource(name = "getReceivedSubstituteRequestList")
    private final GetReceivedSubstituteRequestListUseCase getReceivedSubstituteRequestListUseCase;

    @Resource(name = "getSentSubstituteRequestList")
    private final GetSentSubstituteRequestListUseCase getSentSubstituteRequestListUseCase;

    @Resource(name = "acceptSubstituteRequest")
    private final AcceptSubstituteRequestUseCase acceptSubstituteRequestUseCase;

    @Resource(name = "rejectSubstituteRequest")
    private final RejectSubstituteRequestUseCase rejectSubstituteRequestUseCase;

    @Resource(name = "cancelSubstituteRequest")
    private final CancelSubstituteRequestUseCase cancelSubstituteRequestUseCase;

    @Override
    @GetMapping("/schedules/{scheduleId}/exchangeable-workers")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ExchangeableWorkerResponseDto>>> getExchangeableWorkers(
        @PathVariable Long scheduleId,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(
            getExchangeableWorkersUseCase.execute(actor, scheduleId, pageRequest)
        ));
    }

    @Override
    @PostMapping("/schedules/{scheduleId}/substitute-requests")
    public ResponseEntity<CommonApiResponse<Void>> createSubstituteRequest(
        @PathVariable Long scheduleId,
        @Valid @RequestBody CreateSubstituteRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        createSubstituteRequestUseCase.execute(actor, scheduleId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/workspaces/{workspaceId}/substitute-requests/received")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ReceivedSubstituteRequestResponseDto>>> getReceivedRequests(
        @PathVariable Long workspaceId,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(
            getReceivedSubstituteRequestListUseCase.execute(actor, workspaceId, pageRequest)
        ));
    }

    @Override
    @GetMapping("/users/me/substitute-requests/sent")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<SubstituteRequestResponseDto>>> getSentRequests(
        @RequestParam(required = false) SubstituteRequestStatus status,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(
            getSentSubstituteRequestListUseCase.execute(actor, status, pageRequest)
        ));
    }

    @Override
    @PostMapping("/substitute-requests/{requestId}/accept")
    public ResponseEntity<CommonApiResponse<Void>> acceptRequest(
        @PathVariable Long requestId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        acceptSubstituteRequestUseCase.execute(actor, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/substitute-requests/{requestId}/reject")
    public ResponseEntity<CommonApiResponse<Void>> rejectRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody RejectSubstituteRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        rejectSubstituteRequestUseCase.execute(actor, requestId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/users/me/substitute-requests/{requestId}")
    public ResponseEntity<CommonApiResponse<Void>> cancelRequest(
        @PathVariable Long requestId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        cancelSubstituteRequestUseCase.execute(actor, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
