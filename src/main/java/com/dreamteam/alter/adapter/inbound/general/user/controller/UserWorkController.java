package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetUserActiveWorkspaceListUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 근무 관리", description = "사용자 근무 관련 API")
@RestController
@RequestMapping("/app/users/workspaces")
@RequiredArgsConstructor
public class UserWorkController implements UserWorkControllerSpec {

    private final GetUserActiveWorkspaceListUseCase getUserActiveWorkspaceListUseCase;

    @Operation(summary = "현재 근무중인 업장 목록 조회", description = "사용자가 현재 근무중인 아르바이트 업장 목록을 커서 페이징으로 조회합니다.")
    @GetMapping("/active")
    public ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceListResponseDto>>> getActiveWorkspaceList(
        CursorPageRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        CursorPaginatedApiResponse<UserWorkspaceListResponseDto> response = 
            getUserActiveWorkspaceListUseCase.execute(request, actor);
        
        return ResponseEntity.ok(CommonApiResponse.of(response));
    }

}
