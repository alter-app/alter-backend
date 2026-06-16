package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "APP - 업장 등록 신청 댓글 API")
public interface UserWorkspaceRequestCommentControllerSpec {

	@Operation(summary = "유저 - 댓글 등록")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "댓글 등록 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<Void>> createComment(
		@PathVariable Long workspaceRequestId,
		@Valid @RequestBody CreateWorkspaceRequestCommentRequestDto request
	);

	@Operation(summary = "유저 - 댓글 목록 조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<List<WorkspaceRequestCommentResponseDto>>> getCommentList(
		@PathVariable Long workspaceRequestId
	);
}
