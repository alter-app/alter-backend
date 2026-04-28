package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonCommentResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "MANAGER - 업장 반려 사유 코멘트 API")
public interface ManagerWorkspaceReasonCommentControllerSpec {

    @Operation(summary = "매니저 - 반려 사유 코멘트 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "코멘트 등록 성공"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사유 (B019)")
    })
    ResponseEntity<CommonApiResponse<Void>> createWorkspaceReasonComment(
        @PathVariable Long workspaceId,
        @PathVariable Long reasonId,
        @Valid @RequestBody CreateWorkspaceReasonCommentRequestDto request
    );

    @Operation(summary = "매니저 - 반려 사유 코멘트 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "코멘트 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사유 (B019)")
    })
    ResponseEntity<CommonApiResponse<List<WorkspaceReasonCommentResponseDto>>> getWorkspaceReasonComments(
        @PathVariable Long workspaceId,
        @PathVariable Long reasonId
    );
}
