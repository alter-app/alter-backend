package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.*;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "APP - 대타 요청 관리 API")
public interface UserSubstituteRequestControllerSpec {

    @Operation(summary = "교환 가능한 근무자 조회", description = "특정 스케줄에 대해 교환 가능한 근무자 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 스케줄",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ExchangeableWorkerResponseDto>>> getExchangeableWorkers(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long scheduleId,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "대타 요청 생성", description = "특정 스케줄에 대한 대타 요청을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청",
                        value = "{\"code\" : \"B001\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 스케줄",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createSubstituteRequest(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long scheduleId,
        @Valid @RequestBody CreateSubstituteRequestDto request
    );

    @Operation(summary = "받은 대타 요청 목록 조회", description = "내가 받은 대타 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<ReceivedSubstituteRequestResponseDto>>> getReceivedRequests(
        @Parameter(description = "워크스페이스 ID", example = "1", required = true)
        @PathVariable Long workspaceId,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "보낸 대타 요청 목록 조회", description = "내가 보낸 대타 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CommonApiResponse<CursorPaginatedApiResponse<SubstituteRequestListResponseDto>>> getSentRequests(
        @Parameter(description = "요청 상태", example = "PENDING")
        @RequestParam(required = false) SubstituteRequestStatus status,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "보낸 대타 요청 단일 조회", description = "내가 보낸 대타 요청의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
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
    ResponseEntity<CommonApiResponse<SubstituteRequestDetailResponseDto>> getSentRequestDetail(
        @Parameter(description = "대타 요청 ID", example = "1", required = true)
        @PathVariable Long requestId
    );

    @Operation(summary = "대타 요청 수락", description = "받은 대타 요청을 수락합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수락 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이미 처리된 요청",
                        value = "{\"code\" : \"B001\"}"
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
    ResponseEntity<CommonApiResponse<Void>> acceptRequest(
        @Parameter(description = "대타 요청 ID", example = "1", required = true)
        @PathVariable Long requestId
    );

    @Operation(summary = "대타 요청 거절", description = "받은 대타 요청을 거절합니다. (특정 대상 요청만 가능)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "거절 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "거절할 수 없는 요청",
                        value = "{\"code\" : \"B001\"}"
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
        @Valid @RequestBody RejectSubstituteRequestDto request
    );

    @Operation(summary = "대타 요청 취소", description = "내가 보낸 대타 요청을 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "취소할 수 없는 요청",
                        value = "{\"code\" : \"B001\"}"
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
    ResponseEntity<CommonApiResponse<Void>> cancelRequest(
        @Parameter(description = "대타 요청 ID", example = "1", required = true)
        @PathVariable Long requestId
    );
}

