package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "APP - 근무 스케줄 관리 API")
public interface UserScheduleControllerSpec {

    @Operation(summary = "나의 근무 스케줄 조회", description = "파라미터 조합에 따라 조회가 달라집니다. <br>"+
                                                           "- 인자 없음: 이번 주 스케줄 조회<br>" +
                                                           "- year, month: 해당 월 스케줄 조회 (예상 급여 포함)<br>" +
                                                           "- year, month, day: 해당 일 스케줄 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    })
    ResponseEntity<CommonApiResponse<GetMyScheduleResponseDto>> getMySchedule(
        WorkScheduleInquiryRequestDto request
    );

    @Operation(summary = "업장별 근무 스케줄 조회", description = "year, month 값을 모두 포함해야합니다. 응답에는 나의 총 근무 시간과 예상 급여가 포함됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 업장",
                        value = "{\"code\" : \"B008\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<GetWorkspaceScheduleResponseDto>> getWorkspaceSchedule(
        @Parameter(description = "업장 ID", example = "1", required = true)
        @PathVariable Long workspaceId,
        WorkScheduleInquiryRequestDto request
    );
}
