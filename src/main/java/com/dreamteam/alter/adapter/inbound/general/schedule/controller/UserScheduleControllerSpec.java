package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkspaceScheduleResponseDto;
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

import java.util.List;

@Tag(name = "APP - 근무 스케줄 관리 API")
public interface UserScheduleControllerSpec {

    @Operation(summary = "전체 근무 스케줄 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<MyScheduleResponseDto>>> getMySchedule(
        WorkScheduleInquiryRequestDto request
    );

    @Operation(summary = "업장별 근무 스케줄 조회")
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
    ResponseEntity<CommonApiResponse<List<WorkspaceScheduleResponseDto>>> getWorkspaceSchedule(
        @Parameter(description = "업장 ID", example = "1", required = true)
        @PathVariable Long workspaceId,
        WorkScheduleInquiryRequestDto request
    );
}
