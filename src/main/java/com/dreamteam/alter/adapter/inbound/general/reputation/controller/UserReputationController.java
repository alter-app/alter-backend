package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.*;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.reputation.port.inbound.*;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/reputations")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class UserReputationController implements UserReputationControllerSpec {

    @Resource(name = "getAvailableReputationKeywordList")
    private final GetAvailableReputationKeywordListUseCase getAvailableReputationKeywordList;

    @Resource(name = "userCreateReputation")
    private final AppCreateReputationToUserUseCase appCreateReputationToUserUseCase;

    @Resource(name = "appCreateReputationToWorkspace")
    private final AppCreateReputationToWorkspaceUseCase appCreateReputationToWorkspace;

    @Resource(name = "userGetReputationRequestList")
    private final UserGetReputationRequestListUseCase userGetReputationRequestList;

    @Resource(name = "appDeclineReputationRequest")
    private final AppDeclineReputationRequestUseCase appDeclineReputationRequest;

    @Resource(name = "appAcceptReputationRequest")
    private final AppAcceptReputationRequestUseCase appAcceptReputationRequest;

    @Resource(name = "userGetSentReputationRequestList")
    private final UserGetSentReputationRequestListUseCase userGetSentReputationRequestList;

    @Resource(name = "userCancelReputationRequest")
    private final UserCancelReputationRequestUseCase userCancelReputationRequest;

    @Override
    @GetMapping("/keywords")
    public ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords(
        AvailableReputationKeywordRequestDto keywordRequestDto
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(getAvailableReputationKeywordList.execute(keywordRequestDto)));
    }

    @Override
    @PostMapping("/requests/users")
    public ResponseEntity<CommonApiResponse<Void>> createReputationToUser(
        @Valid @RequestBody CreateReputationToUserRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        appCreateReputationToUserUseCase.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/requests/workspaces")
    public ResponseEntity<CommonApiResponse<Void>> createReputationToWorkspace(
        @Valid @RequestBody CreateReputationToWorkspaceRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        appCreateReputationToWorkspace.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/requests")
    public ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getReputationRequestList(
        UserReputationRequestFilterDto filter,
        CursorPageRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(userGetReputationRequestList.execute(actor, filter, request));
    }

    @Override
    @PatchMapping("/requests/{requestId}/decline")
    public ResponseEntity<CommonApiResponse<Void>> declineReputationRequest(@PathVariable Long requestId) {
        AppActor actor = AppActionContext.getInstance().getActor();

        appDeclineReputationRequest.execute(actor, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<CommonApiResponse<Void>> acceptReputationRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody AcceptReputationRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        appAcceptReputationRequest.execute(actor, requestId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/requests/sent")
    public ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getSentReputationRequestList(
        UserReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(userGetSentReputationRequestList.execute(actor, filter, pageRequest));
    }

    @Override
    @PatchMapping("/requests/sent/{requestId}/cancel")
    public ResponseEntity<CommonApiResponse<Void>> cancelSentReputationRequest(@PathVariable Long requestId) {
        AppActor actor = AppActionContext.getInstance().getActor();

        userCancelReputationRequest.execute(actor, requestId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
