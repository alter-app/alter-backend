package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 근무 관리", description = "사용자 근무 관련 API")
public interface UserWorkControllerSpec {

    @Operation(summary = "현재 근무중인 업장 목록 조회", description = "사용자가 현재 근무중인 아르바이트 업장 목록을 커서 페이징으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 목록 조회 성공"),
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceListResponseDto>>> getActiveWorkspaceList(
        CursorPageRequestDto request
    );

}
