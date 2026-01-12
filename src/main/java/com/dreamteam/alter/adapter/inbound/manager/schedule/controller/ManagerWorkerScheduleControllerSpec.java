package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.UpdateWorkerScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "MANAGER - 근무자 스케줄 관리 API")
public interface ManagerWorkerScheduleControllerSpec {

	@Operation(summary = "매니저 - 근무자 고정 스케줄 등록", description = "근무자의 요일별 고정 근무 시간을 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "고정 스케줄 등록 성공"),
		@ApiResponse(responseCode = "400", description = "400 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "시작시간은 종료 시간보다 늦을 수 없습니다.",
						value = "{\"code\" : \"B001\"}"
					),
				})),
		@ApiResponse(responseCode = "404", description = "404 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "존재하지 않는 업장입니다.",
						value = "{\"code\" : \"B019\"}"
					),
					@ExampleObject(
						name = "해당업장에 근무하는 근무자가 아닙니다.",
						value = "{\"code\" : \"B019\"}"
					),
				})),
		@ApiResponse(responseCode = "409", description = "409 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "같은 요일에 겹치는 근무 시간이 존재합니다.",
						value = "{\"code\" : \"B020\"}"
					),
				})),
	})
	ResponseEntity<CommonApiResponse<Void>> createWorkerSchedule(
		@PathVariable Long workspaceId,
		@RequestBody @Valid CreateWorkerScheduleRequestDto requests
	);

	@Operation(summary = "매니저 - 근무자 고정 스케줄 수정", description = "근무자의 요일별 고정 근무 시간을 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "고정 스케줄 수정 성공"),
		@ApiResponse(responseCode = "400", description = "400 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "시작시간은 종료 시간보다 늦을 수 없습니다.",
						value = "{\"code\" : \"B001\"}"
					),
				})),
		@ApiResponse(responseCode = "404", description = "404 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "존재하지 않는 업장입니다.",
						value = "{\"code\" : \"B019\"}"
					),
					@ExampleObject(
						name = "수정할 스케줄을 찾을 수 없습니다.",
						value = "{\"code\" : \"B019\"}"
					),
				})),
		@ApiResponse(responseCode = "409", description = "409 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "같은 요일에 겹치는 근무 시간이 존재합니다.",
						value = "{\"code\" : \"B020\"}"
					),
				})),
	})
	ResponseEntity<CommonApiResponse<Void>> updateWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerScheduleId,
		@RequestBody @Valid UpdateWorkerScheduleRequestDto request
	);

	@Operation(summary = "매니저 - 근무자 고정 스케줄 삭제", description = "근무자의 요일별 고정 근무 시간을 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "고정 스케줄 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "404 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "존재하지 않는 업장입니다.",
						value = "{\"code\" : \"B019\"}"
					),
					@ExampleObject(
						name = "삭제할 스케줄을 찾을 수 없습니다.",
						value = "{\"code\" : \"B019\"}"
					),
				})),
	})
	ResponseEntity<CommonApiResponse<Void>> deleteWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerScheduleId
	);
}
