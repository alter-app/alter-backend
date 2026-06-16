package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.UpdateWorkspaceRequestStatusDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

	@Operation(summary = "업장 등록 신청 상태 변경", description = "관리자가 업장 등록 신청을 승인(ACTIVATED) 또는 반려(REVOKED) 처리합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "상태 변경 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 상태 값"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 업장 등록 신청")
	})
	ResponseEntity<CommonApiResponse<Void>> updateStatus(
		@Parameter(description = "업장 등록 신청 ID", example = "1") @PathVariable Long workspaceRequestId,
		@Valid @RequestBody UpdateWorkspaceRequestStatusDto request
	);
}
