package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserActiveWorkspaceListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserWorkspaceWorkerListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/users/me/workspaces")
@RequiredArgsConstructor
public class UserWorkController implements UserWorkControllerSpec {

    private final GetUserActiveWorkspaceListUseCase getUserActiveWorkspaceListUseCase;
    private final GetUserWorkspaceWorkerListUseCase getUserWorkspaceWorkerListUseCase;

    @GetMapping
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceListResponseDto>>> getActiveWorkspaceList(
        CursorPageRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        CursorPaginatedApiResponse<UserWorkspaceListResponseDto> response = 
            getUserActiveWorkspaceListUseCase.execute(request, actor);
        
        return ResponseEntity.ok(CommonApiResponse.of(response));
    }

    @Override
    @GetMapping("/{workspaceId}/workers")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceWorkerListResponseDto>>> getWorkspaceWorkerList(
        @PathVariable Long workspaceId,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getUserWorkspaceWorkerListUseCase.execute(actor, workspaceId, pageRequest)));
    }

}
