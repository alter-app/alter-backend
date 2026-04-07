package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "ADMIN - 업장 등록 신청 관리 API")
public interface AdminWorkspaceRequestControllerSpec {

    @Operation(summary = "업장 등록 신청 목록 조회", description = "관리자가 업장 등록 신청 목록을 페이징으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<PaginatedResponseDto<AdminWorkspaceRequestListResponseDto>>> getWorkspaceRequestList(
        PageRequestDto request
    );

    @Operation(summary = "업장 등록 신청 상세 조회", description = "관리자가 업장 등록 신청 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
    })
    ResponseEntity<CommonApiResponse<AdminWorkspaceRequestResponseDto>> getWorkspaceRequest(
        @Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId
    );

    @Operation(summary = "업장 등록 신청 승인", description = "관리자가 업장 등록 신청을 승인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 승인 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
    })
    ResponseEntity<CommonApiResponse<Void>> approve(
        @Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId
    );
}
