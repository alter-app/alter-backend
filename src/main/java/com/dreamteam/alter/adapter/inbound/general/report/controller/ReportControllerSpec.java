package com.dreamteam.alter.adapter.inbound.general.report.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.report.dto.*;
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

@Tag(name = "APP - 신고 관련 API")
public interface ReportControllerSpec {

    @Operation(summary = "신고 생성", description = "사용자가 신고를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 생성 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (유효하지 않은 신고 타입, targetId 누락 등)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 대상",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createReport(
        @Valid @RequestBody CreateReportRequestDto request
    );

    @Operation(summary = "내 신고 목록 조회", description = "사용자의 신고 목록을 커서 페이징으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (유효하지 않은 커서, 페이지 크기 등)",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CursorPaginatedApiResponse<ReportListResponseDto>> getMyReportList(
        CursorPageRequestDto request,
        ReportListFilterDto filter
    );

    @Operation(summary = "내 신고 상세 조회", description = "사용자의 신고 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 신고",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "권한이 없는 신고",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<ReportDetailResponseDto>> getMyReportDetail(
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId
    );

    @Operation(summary = "내 신고 취소", description = "사용자의 신고를 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 취소 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 신고",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "권한이 없는 신고",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "취소할 수 없는 신고 상태",
                        value = "{\"code\" : \"B012\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> cancelMyReport(
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId
    );
}
