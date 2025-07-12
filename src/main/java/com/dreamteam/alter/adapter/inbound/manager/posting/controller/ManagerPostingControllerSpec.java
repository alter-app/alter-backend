package com.dreamteam.alter.adapter.inbound.manager.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 업장 관리자 공고 관리 API")
public interface ManagerPostingControllerSpec {

    @Operation(summary = "매니저 - 공고 지원 목록 조회 (커서 페이징)", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 지원 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<PostingApplicationListResponseDto>> getPostingApplicationListWithCursor(
        CursorPageRequestDto request,
        PostingApplicationListFilterDto filter
    );

    @Operation(summary = "매니저 - 공고 지원 상세 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 지원 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "공고 지원 정보 찾을 수 없음",
                        value = "{\"code\" : \"B012\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<PostingApplicationResponseDto>> getPostingApplicationDetail(
        @PathVariable Long postingApplicationId
    );

    @Operation(summary = "매니저 - 공고 지원 상태 변경", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 지원 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "SHORTLISTED, ACCEPTED, REJECTED 상태로만 변경 요청 가능",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "공고 지원 정보 찾을 수 없음",
                        value = "{\"code\" : \"B012\"}"
                    ),
                    @ExampleObject(
                        name = "ACCEPTED, CANCELLED, REJECTED, EXPIRED 상태의 지원서는 상태 변경 불가",
                        value = "{\"code\" : \"B017\"}"
                    ),
                    @ExampleObject(
                        name = "이미 근무중인 사용자입니다.",
                        value = "{\"code\" : \"B018\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updatePostingApplicationStatus(
        @PathVariable Long postingApplicationId,
        @RequestBody UpdatePostingApplicationStatusRequestDto request
    );

}
