package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerRejectSubstituteRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerApproveSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetSubstituteRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerRejectSubstituteRequestUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
@RequestMapping("/manager")
public class ManagerSubstituteRequestController implements ManagerSubstituteRequestControllerSpec {

    @Resource(name = "managerGetSubstituteRequestList")
    private final ManagerGetSubstituteRequestListUseCase managerGetSubstituteRequestListUseCase;

    @Resource(name = "managerApproveSubstituteRequest")
    private final ManagerApproveSubstituteRequestUseCase managerApproveSubstituteRequestUseCase;

    @Resource(name = "managerRejectSubstituteRequest")
    private final ManagerRejectSubstituteRequestUseCase managerRejectSubstituteRequestUseCase;

    @Override
    @GetMapping("/workspaces/{workspaceId}/substitute-requests")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerSubstituteRequestResponseDto>>> getRequests(
        @PathVariable Long workspaceId,
        ManagerSubstituteRequestListFilterDto filter,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(
            managerGetSubstituteRequestListUseCase.execute(actor, workspaceId, filter, pageRequest)
        ));
    }

    @Override
    @PostMapping("/substitute-requests/{requestId}/approve")
    public ResponseEntity<CommonApiResponse<Void>> approveRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody ApproveSubstituteRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerApproveSubstituteRequestUseCase.execute(actor, requestId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/substitute-requests/{requestId}/reject")
    public ResponseEntity<CommonApiResponse<Void>> rejectRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody ManagerRejectSubstituteRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerRejectSubstituteRequestUseCase.execute(actor, requestId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
