package com.dreamteam.alter.adapter.inbound.admin.terms.controller;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
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

import java.util.Map;

@Tag(name = "ADMIN - 약관 관리 API")
public interface AdminTermsControllerSpec {

    @Operation(summary = "약관 목록 조회", description = "관리자가 약관 목록을 오프셋 페이징으로 조회합니다. 유형, 상태로 필터링할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 목록 조회 성공")
    })
    ResponseEntity<PaginatedResponseDto<AdminTermsListItemResponseDto>> getTermsList(
        PageRequestDto pageRequest,
        TermsListFilterDto filter
    );

    @Operation(summary = "약관 생성", description = "관리자가 새 약관을 DRAFT 상태로 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "약관 생성 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "유효성 검증 실패",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Map<String, Long>>> createTerms(
        @Valid @RequestBody AdminCreateTermsRequestDto request
    );

    @Operation(summary = "약관 상세 조회", description = "관리자가 약관 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 약관",
                        value = "{\"code\" : \"B019\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<AdminTermsDetailResponseDto>> getTermsDetail(
        @Parameter(description = "약관 ID", example = "1") @PathVariable Long id
    );

    @Operation(summary = "약관 수정", description = "관리자가 DRAFT 상태의 약관을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 수정 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 약관",
                        value = "{\"code\" : \"B019\"}"
                    ),
                    @ExampleObject(
                        name = "수정 불가 상태 (DRAFT가 아님)",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateTerms(
        @Parameter(description = "약관 ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody AdminUpdateTermsRequestDto request
    );

    @Operation(summary = "약관 게시", description = "관리자가 DRAFT 상태의 약관을 게시합니다. 동일 유형의 기존 PUBLISHED 약관은 자동으로 DEPRECATED 처리됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 게시 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 약관",
                        value = "{\"code\" : \"B019\"}"
                    ),
                    @ExampleObject(
                        name = "게시 불가 상태 (DRAFT가 아님)",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> publishTerms(
        @Parameter(description = "약관 ID", example = "1") @PathVariable Long id
    );
}
