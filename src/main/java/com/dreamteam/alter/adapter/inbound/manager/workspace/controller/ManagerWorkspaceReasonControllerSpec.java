package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "MANAGER - 업장 반려 사유 API")
public interface ManagerWorkspaceReasonControllerSpec {

	@Operation(summary = "매니저 - 반려 사유 목록 조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "반려 사유 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<List<WorkspaceReasonResponseDto>>> getWorkspaceReasonList(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId
	);
}