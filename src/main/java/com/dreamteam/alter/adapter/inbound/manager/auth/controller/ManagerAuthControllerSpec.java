package com.dreamteam.alter.adapter.inbound.manager.auth.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "MANAGER - 인증 관련 API")
public interface ManagerAuthControllerSpec {

    @Operation(summary = "Firebase 커스텀 토큰 발급")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firebase 커스텀 토큰 발급 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommonApiResponse<FirebaseCustomTokenResponseDto>> generateFirebaseCustomToken();

}
