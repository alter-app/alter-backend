package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerRejectSubstituteRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerSubstituteRequestResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 대타 요청 관리 API")
public interface ManagerSubstituteRequestControllerSpec {

    @Operation(summary = "대타 요청 목록 조회", description = "매니저가 관리하는 업장의 대타 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "관리 중인 업장이 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 워크스페이스",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ManagerSubstituteRequestResponseDto>>> getRequests(
        ManagerSubstituteRequestListFilterDto filter,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "대타 요청 승인", description = "대타 요청을 승인하고 실제 스케줄을 교환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "승인 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "승인할 수 없는 상태",
                        value = "{\"code\" : \"B001\"}"
                    )
                })),
        @ApiResponse(responseCode = "403", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "관리 중인 업장이 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 요청",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> approveRequest(
        @Parameter(description = "대타 요청 ID", example = "1", required = true)
        @PathVariable Long requestId,
        @Valid @RequestBody ApproveSubstituteRequestDto request
    );

    @Operation(summary = "대타 요청 거절", description = "대타 요청을 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "거절 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "거절할 수 없는 상태",
                        value = "{\"code\" : \"B001\"}"
                    )
                })),
        @ApiResponse(responseCode = "403", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "관리 중인 업장이 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 요청",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> rejectRequest(
        @Parameter(description = "대타 요청 ID", example = "1", required = true)
        @PathVariable Long requestId,
        @Valid @RequestBody ManagerRejectSubstituteRequestDto request
    );
}

