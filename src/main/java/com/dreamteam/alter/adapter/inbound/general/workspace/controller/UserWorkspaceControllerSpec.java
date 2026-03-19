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

@Tag(name = "GENERAL - 업장 API")
public interface UserWorkspaceControllerSpec {

    @Operation(summary = "업장 등록 신청")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업장 등록 신청 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> createWorkspace(
        @RequestBody @Valid CreateWorkspaceRequestDto request
    );
}
