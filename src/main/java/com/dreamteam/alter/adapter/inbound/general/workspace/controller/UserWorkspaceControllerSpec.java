package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 업장 API")
public interface UserWorkspaceControllerSpec {

    @Operation(summary = "업장 등록 신청")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 파일입니다. (INVALID_FILE)"),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다. (FORBIDDEN)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일입니다. (FILE_NOT_FOUND)"),
        @ApiResponse(responseCode = "409", description = "이미 연결된 파일입니다. (FILE_ALREADY_ATTACHED)")
    })
    ResponseEntity<CommonApiResponse<Void>> createWorkspace(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    );
}
