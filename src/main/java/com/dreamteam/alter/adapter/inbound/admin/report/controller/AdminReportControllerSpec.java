package com.dreamteam.alter.adapter.inbound.admin.report.controller;

import com.dreamteam.alter.adapter.inbound.admin.report.dto.AdminUpdateReportStatusRequestDto;
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

@Tag(name = "ADMIN - 관리자 신고 관리 API")
public interface AdminReportControllerSpec {

    @Operation(summary = "전체 신고 목록 조회", description = "관리자가 모든 신고 목록을 커서 페이징으로 조회합니다.")
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
    ResponseEntity<CursorPaginatedApiResponse<ReportListResponseDto>> getReportList(
        CursorPageRequestDto request,
        ReportListFilterDto filter
    );

    @Operation(summary = "신고 상세 조회", description = "관리자가 신고 상세 정보를 조회합니다.")
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
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<ReportDetailResponseDto>> getReportDetail(
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId
    );

    @Operation(summary = "신고 상태 변경", description = "관리자가 신고 상태를 변경하고 코멘트를 남깁니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 상태 변경 성공"),
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
                        name = "잘못된 요청 (유효하지 않은 상태 값 등)",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateReportStatus(
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId,
        @Valid @RequestBody AdminUpdateReportStatusRequestDto request
    );

    @Operation(summary = "신고 삭제", description = "관리자가 신고를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 신고",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> deleteReport(
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId
    );
}
