package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AcceptReputationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserReputationRequestFilterDto;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "APP - 사용자 평판 관련 API")
public interface UserReputationControllerSpec {

    @Operation(summary = "평판 키워드 조회", description = "사용 가능한 평판 키워드를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 키워드 조회 성공")
    })
    ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords(
        AvailableReputationKeywordRequestDto keywordRequestDto
    );

    @Operation(summary = "사용자에 대한 평판 생성", description = "사용자에 대한 평판을 키워드와 함께 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 생성 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (유효하지 않은 요청 타입, workspaceId 누락 등)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 업장",
                        value = "{\"code\" : \"B008\"}"
                    ),
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (평판 키워드, 업장 근무자 등)",
                        value = "{\"code\" : \"B019\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createReputationToUser(@Valid @RequestBody CreateReputationToUserRequestDto request);

    @Operation(summary = "업장에 대한 평판 생성", description = "업장에 대한 평판을 키워드와 함께 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 생성 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (평판 키워드 개수 등)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 업장",
                        value = "{\"code\" : \"B008\"}"
                    ),
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (평판 키워드, 업장 근무자 등)",
                        value = "{\"code\" : \"B019\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createReputationToWorkspace(@Valid @RequestBody CreateReputationToWorkspaceRequestDto request);

    @Operation(summary = "평판 요청 목록 조회", description = "사용자의 평판 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 요청 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 커서 페이징 요청",
                        value = "{\"code\" : \"B005\"}"
                    )
                }))
    })
    ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getReputationRequestList(
        UserReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "평판 요청 거절", description = "평판 요청을 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 요청 거절 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (평판 요청)",
                        value = "{\"code\" : \"B019\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> declineReputationRequest(@PathVariable Long requestId);

    @Operation(summary = "평판 요청 수락", description = "평판 요청을 수락하고 상대방에 대한 평판을 작성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 요청 수락 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (평판 요청)",
                        value = "{\"code\" : \"B019\"}"
                    ),
                    @ExampleObject(
                        name = "잘못된 요청 (평판 키워드 개수 등)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (평판 키워드)",
                        value = "{\"code\" : \"B019\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> acceptReputationRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody AcceptReputationRequestDto request
    );

    @Operation(summary = "보낸 평판 요청 목록 조회", description = "사용자가 보낸 평판 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "보낸 평판 요청 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 커서 페이징 요청",
                        value = "{\"code\" : \"B005\"}"
                    )
                }))
    })
    ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getSentReputationRequestList(
        UserReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "보낸 평판 요청 취소", description = "사용자가 보낸 평판 요청을 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평판 요청 취소 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음 (취소할 수 있는 평판 요청)",
                        value = "{\"code\" : \"B019\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> cancelSentReputationRequest(@PathVariable Long requestId);

}
