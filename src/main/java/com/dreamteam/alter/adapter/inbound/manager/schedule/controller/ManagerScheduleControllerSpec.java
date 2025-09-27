package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.AssignWorkerRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.CreateWorkScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerWorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ManagerScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerRequestDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MANAGER - 근무 스케줄 관리 API")
public interface ManagerScheduleControllerSpec {

    @Operation(summary = "스케줄 생성", description = "새로운 근무 스케줄을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 생성 성공"),
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
    ResponseEntity<CommonApiResponse<Void>> createSchedule(
        @Valid @RequestBody CreateWorkScheduleRequestDto request
    );

    @Operation(summary = "스케줄 목록 조회", description = "특정 워크스페이스의 월별 스케줄 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<ManagerScheduleResponseDto>>> getScheduleList(
        ManagerWorkScheduleInquiryRequestDto request
    );

    @Operation(summary = "스케줄 수정", description = "기존 스케줄의 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 수정 성공"),
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
                        name = "요청한 리소스를 찾을 수 없음",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateSchedule(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long shiftId,
        @Valid @RequestBody UpdateWorkScheduleRequestDto request
    );

    @Operation(summary = "스케줄 삭제", description = "기존 스케줄을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> deleteSchedule(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long shiftId
    );

    @Operation(summary = "근무자 배정", description = "스케줄에 근무자를 배정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무자 배정 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"code\" : \"B011\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> assignWorker(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long shiftId,
        @Valid @RequestBody AssignWorkerRequestDto request
    );

    @Operation(summary = "근무자 변경", description = "스케줄의 근무자를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무자 변경 성공"),
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
        @ApiResponse(responseCode = "403", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "관리 중인 업장이 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateWorker(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long shiftId,
        @Valid @RequestBody UpdateWorkerRequestDto request
    );

    @Operation(summary = "근무자 제거", description = "스케줄에서 근무자를 제거합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무자 제거 성공"),
        @ApiResponse(responseCode = "403", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "관리 중인 업장이 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "요청한 리소스를 찾을 수 없음",
                        value = "{\"code\" : \"B020\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> removeWorker(
        @Parameter(description = "스케줄 ID", example = "1", required = true)
        @PathVariable Long shiftId
    );
}
