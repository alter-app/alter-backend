package com.dreamteam.alter.adapter.inbound.manager.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.application.reputation.usecase.WorkspaceGetReputationRequestList;
import com.dreamteam.alter.domain.reputation.port.inbound.GetAvailableReputationKeywordListUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceCreateReputationToUserUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceDeclineReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceGetReputationRequestListUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/reputations")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class WorkspaceReputationController implements WorkspaceReputationControllerSpec {

    @Resource(name = "getAvailableReputationKeywordList")
    private final GetAvailableReputationKeywordListUseCase getAvailableReputationKeywordList;

    @Resource(name = "workspaceCreateReputationToUser")
    private final WorkspaceCreateReputationToUserUseCase workspaceCreateReputationToUser;

    @Resource(name = "workspaceGetReputationRequestList")
    private final WorkspaceGetReputationRequestListUseCase workspaceGetReputationRequestList;

    @Resource(name = "workspaceDeclineReputationRequest")
    private final WorkspaceDeclineReputationRequestUseCase workspaceDeclineReputationRequest;

    @Override
    @GetMapping("/keywords")
    public ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords(
        AvailableReputationKeywordRequestDto keywordRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(getAvailableReputationKeywordList.execute(keywordRequestDto)));
    }

    @Override
    @PostMapping("/requests/users")
    public ResponseEntity<CommonApiResponse<Void>> createReputationToUser(
        @Valid @RequestBody CreateReputationToUserRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        workspaceCreateReputationToUser.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/requests")
    public ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getReputationRequestList(
        ReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(workspaceGetReputationRequestList.execute(actor, filter, pageRequest));
    }

    @Override
    @PatchMapping("/requests/{requestId}/decline")
    public ResponseEntity<CommonApiResponse<Void>> declineReputationRequest(@PathVariable Long requestId) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        workspaceDeclineReputationRequest.execute(actor, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
