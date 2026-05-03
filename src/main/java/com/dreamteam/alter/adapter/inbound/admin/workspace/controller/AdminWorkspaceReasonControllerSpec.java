package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.CreateWorkspaceReasonDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "ADMIN - 업장 등록 신청 반려 사유 API")
public interface AdminWorkspaceReasonControllerSpec {

	@Operation(summary = "반려 사유 목록 조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "반려 사유 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<List<WorkspaceReasonResponseDto>>> getWorkspaceReasonList(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId
	);

	@Operation(summary = "업장 등록 신청 반려 사유 등록", description = "관리자가 업장 등록 신청에 대한 반려 사유를 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "반려 사유 등록 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<Void>> createReason(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId,
		@Valid @RequestBody CreateWorkspaceReasonDto request
	);
}
