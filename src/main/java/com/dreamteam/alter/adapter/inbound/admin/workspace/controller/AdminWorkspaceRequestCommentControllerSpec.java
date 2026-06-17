package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "ADMIN - 업장 등록 신청 댓글 API")
public interface AdminWorkspaceRequestCommentControllerSpec {

	@Operation(summary = "댓글 목록 조회", description = "관리자가 업장 등록 신청의 댓글 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<List<WorkspaceRequestCommentResponseDto>>> getCommentList(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId
	);

	@Operation(summary = "댓글 등록", description = "관리자가 업장 등록 신청에 댓글을 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "댓글 등록 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<Void>> createComment(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId,
		@Valid @RequestBody CreateWorkspaceRequestCommentRequestDto request
	);
}
