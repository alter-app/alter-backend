package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminBusinessTypeRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminBusinessTypeResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "ADMIN - 업종(BusinessType) 관리 API")
public interface AdminBusinessTypeControllerSpec {

    @Operation(summary = "업종 목록 조회", description = "관리자가 등록된 업종 전체 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<AdminBusinessTypeResponseDto>>> getBusinessTypeList();

    @Operation(summary = "업종 생성", description = "관리자가 새 업종을 등록합니다. 이름은 중복될 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "업종 생성 성공"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 업종 (CONFLICT)")
    })
    ResponseEntity<CommonApiResponse<AdminBusinessTypeResponseDto>> createBusinessType(
        @Valid @RequestBody AdminBusinessTypeRequestDto request
    );

    @Operation(summary = "업종 수정", description = "관리자가 업종의 이름/설명을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 수정 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 업종 (NOT_FOUND)"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 업종 (CONFLICT)")
    })
    ResponseEntity<CommonApiResponse<Void>> updateBusinessType(
        @PathVariable Long id,
        @Valid @RequestBody AdminBusinessTypeRequestDto request
    );

    @Operation(summary = "업종 삭제", description = "관리자가 업종을 삭제합니다. '기타' 업종 또는 업장/신청에서 사용 중인 업종은 삭제할 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 업종 (NOT_FOUND)"),
        @ApiResponse(responseCode = "409", description = "'기타' 업종 또는 사용 중인 업종 (CONFLICT)")
    })
    ResponseEntity<CommonApiResponse<Void>> deleteBusinessType(
        @PathVariable Long id
    );
}
