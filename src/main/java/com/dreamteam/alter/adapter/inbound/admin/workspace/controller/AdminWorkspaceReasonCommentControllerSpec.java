package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminCreateWorkspaceReasonCommentRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "ADMIN - 업장 등록 신청 반려 사유 코멘트 API")
public interface AdminWorkspaceReasonCommentControllerSpec {

	@Operation(summary = "업장 등록 신청 반려 사유 코멘트 등록", description = "관리자가 업장 등록 신청의 반려 사유에 코멘트를 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "코멘트 등록 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청 또는 반려 사유")
	})
	ResponseEntity<CommonApiResponse<Void>> createComment(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId,
		@Parameter(description = "반려 사유 ID", example = "1") @PathVariable Long reasonId,
		@Valid @RequestBody AdminCreateWorkspaceReasonCommentRequestDto request
	);
}
