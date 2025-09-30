package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "MANAGER - 업장 관리 API")
public interface ManagerWorkspaceControllerSpec {

    @Operation(summary = "매니저 - 내가 관리하는 업장 목록 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리하는 업장 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<ManagerWorkspaceListResponseDto>>> getWorkspaceList();

    @Operation(summary = "매니저 - 내가 관리하는 업장 상세 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리하는 업장 상세 조회 성공")
    })
    ResponseEntity<CommonApiResponse<ManagerWorkspaceResponseDto>> getWorkspaceDetail(
        @PathVariable Long workspaceId
    );

    @Operation(summary = "매니저 - 업장 근무자 목록 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무자 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerWorkspaceWorkerListResponseDto>>> getWorkspaceWorkerList(
        @PathVariable Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequestDto pageRequest
    );


    @Operation(summary = "매니저 - 업장 점주/매니저 목록 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "점주/매니저 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerWorkspaceManagerListResponseDto>>> getWorkspaceManagerList(
        @PathVariable Long workspaceId,
        CursorPageRequestDto pageRequest
    );

    // 업장 근무자 상세 정보 조회

    // 업장 근무자 상태 변경

    // 내가 관리하는 업장 제거 (다만 해당 업장의 관리자가 1인 이하일 경우 제거 불가 예외 처리) - 후순위

}
