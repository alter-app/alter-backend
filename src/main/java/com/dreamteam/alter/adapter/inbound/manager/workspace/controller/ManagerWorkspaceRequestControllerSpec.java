package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "MANAGER - 업장 등록 API")
public interface ManagerWorkspaceRequestControllerSpec {

    @Operation(summary = "매니저 - 업장 등록 신청")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 파일입니다. (INVALID_FILE)"),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다. (FORBIDDEN)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일입니다. (FILE_NOT_FOUND)"),
        @ApiResponse(responseCode = "409", description = "이미 연결된 파일입니다. (FILE_ALREADY_ATTACHED)")
    })
    ResponseEntity<CommonApiResponse<Void>> createWorkspaceRequest(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    );

    @Operation(summary = "매니저 - 업장 등록 신청 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 목록 조회 성공"),
    })
    ResponseEntity<CommonApiResponse<List<WorkspaceRequestListResponseDto>>> getWorkspaceRequestList();


    @Operation(summary = "매니저 - 업장 등록 신청 상세 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "업장 등록 신청 찾을 수 없습니다."),
    })
    ResponseEntity<CommonApiResponse<WorkspaceRequestResponseDto>> getWorkspaceRequestDetail(
        @PathVariable Long workspaceRequestId
    );

    @Operation(summary = "매니저 - 업장 등록 신청 취소")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 취소 성공"),
        @ApiResponse(responseCode = "404", description = "등록 신청한 업장을 찾을 수 없습니다. (NOT_FOUND)"),
        @ApiResponse(responseCode = "409", description = "취소할 수 없는 상태입니다. (CONFLICT)")
    })
    ResponseEntity<CommonApiResponse<Void>> cancelWorkspaceRequest(
        @PathVariable Long workspaceRequestId
    );
}
