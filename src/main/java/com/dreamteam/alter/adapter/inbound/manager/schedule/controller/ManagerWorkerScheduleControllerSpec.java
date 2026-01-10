package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
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
		@ApiResponse(responseCode = "400", description = "실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "존재하지 않는 업장입니다.",
						value = "{\"code\" : \"B008\"}"
					),
					@ExampleObject(
						name = "존재하지 않는 근무자",
						value = "{\"code\" : \"B011\"}"
					),
					@ExampleObject(
						name = "해당업장에 근무하는 근무자가 아닙니다.",
						value = "{\"code\" : \"B022\"}"
					),
					@ExampleObject(
						name = "시작시간은 종료 시간보다 늦을 수 없습니다.",
						value = "{\"code\" : \"B023\"}"
					)
				})),
	})
	ResponseEntity<CommonApiResponse<Void>> createWorkerSchedule(
		@PathVariable Long workspaceId,
		@PathVariable Long workerId,
		@Valid @RequestBody List<CreateWorkerScheduleRequestDto> requests
	);
}
