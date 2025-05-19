package com.dreamteam.alter.adapter.inbound.general.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "App - 공고 API")
public interface PostingControllerSpec {

    @Operation(summary = "공고 등록", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 등록 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "서버 내부 오류",
                        value = "{\"code\" : \"C001\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createPosting(@Valid @RequestBody CreatePostingRequestDto request);

    @Operation(summary = "공고 리스트 조회 (커서 페이징)", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 리스트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "등록되지 않은 키워드로 요청",
                        value = "{\"code\" : \"B006\"}"
                    ),
                    @ExampleObject(
                        name = "요청에 키워드가 포함되지 않은 경우",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CursorPaginatedApiResponse<PostingListResponseDto>> getPostingsWithCursor(CursorPageRequestDto request);

    @Operation(summary = "공고 상세 조회", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공고 리스트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 공고",
                        value = "{\"code\" : \"B007\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<PostingDetailResponseDto>> getPostingDetail(Long postingId);

}
