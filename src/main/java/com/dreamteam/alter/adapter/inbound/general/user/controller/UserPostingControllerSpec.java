package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 사용자 지원한 공고 관련 API")
public interface UserPostingControllerSpec {

    @Operation(summary = "사용자 공고 지원 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지원 목록 조회 성공")
    })
    ResponseEntity<PaginatedResponseDto<UserPostingApplicationListResponseDto>> getMyPostingApplications(PageRequestDto pageRequest);

    @Operation(summary = "사용자 공고 지원 상태 업데이트 (지원 취소)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지원 상태 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "상태 변경 요청이 CANCELLED 가 아닌 경우",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "정보에 해당하는 지원 내역이 없는 경우",
                        value = "{\"code\" : \"B012\"}"
                    ),
                    @ExampleObject(
                        name = "이미 CANCELLED 상태인 경우",
                        value = "{\"code\" : \"B013\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateMyPostingApplicationStatus(
        @PathVariable @Min(1) Long applicationId,
        @RequestBody @Valid UpdateUserPostingApplicationStatusRequestDto request
    );

}
