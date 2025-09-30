package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceWorkerListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserWorkspaceManagerListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "APP - 사용자 자신이 근무중인 아르바이트 조회")
public interface UserWorkControllerSpec {

    @Operation(summary = "현재 근무중인 업장 목록 조회", description = "사용자가 현재 근무중인 아르바이트 업장 목록을 커서 페이징으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 목록 조회 성공"),
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceListResponseDto>>> getActiveWorkspaceList(
        CursorPageRequestDto request
    );

    @Operation(summary = "근무중인 업장의 근무자 목록 조회", description = "사용자가 근무중인 업장의 근무자 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무자 목록 조회 성공"),
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceWorkerListResponseDto>>> getWorkspaceWorkerList(
        Long workspaceId,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "근무중인 업장의 점주/매니저 목록 조회", description = "사용자가 근무중인 업장의 점주/매니저 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "점주/매니저 목록 조회 성공"),
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<UserWorkspaceManagerListResponseDto>>> getWorkspaceManagerList(
        Long workspaceId,
        CursorPageRequestDto pageRequest
    );

}
