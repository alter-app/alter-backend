package com.dreamteam.alter.adapter.inbound.manager.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.FixedWorkerScheduleResponseDto;
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

@Tag(name = "MANAGER - 근무자 고정 스케줄 관리 API")
public interface ManagerFixedWorkerScheduleControllerSpec {

	@Operation(summary = "매니저 - 근무자 고정 스케줄 등록", description = """
		매주 또는 매월 n번째 요일의 고정 근무 규칙과 적용 기간을 등록합니다.
		매월의 다섯째 요일이 없으면 그 달은 건너뜁니다. 적용 기간은 근무 시작일 기준 양끝을 포함합니다.
		종료일 null은 무기한입니다. 반복·기간 필드 전체 생략은 기존 무기한 매주 규칙입니다.
		등록은 규칙만 저장하며 실제 근무는 업장별 지정일에 다음 달 배치로 생성합니다.
		매월 예시: recurrenceType=MONTHLY, weekOfMonth=2, startDayOfWeek=TUESDAY,
		endDayOfWeek=TUESDAY, startTime=09:00:00, endTime=18:00:00,
		startDate=2026-10-01, endDate=2027-03-31.
		""")
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

	@Operation(summary = "매니저 - 근무자 고정 스케줄 수정", description = """
		고정 근무의 요일·시간·반복·적용 기간을 수정합니다. 이미 생성된 실제 근무는 보존합니다.
		반복·기간 필드 전체 생략은 기존 반복·기간 보존입니다.
		반복·기간 변경 시 recurrenceType과 startDate를 함께 보내며 MONTHLY는 weekOfMonth도 필수입니다.
		WEEKLY의 weekOfMonth는 null입니다. endDate=null로 종료일을 해제할 수 있습니다.
		일요일 밤→월요일 아침처럼 종료 요일을 명시한 주 경계 근무를 지원합니다.
		""")
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

	@Operation(summary = "매니저 - 근무자 고정 스케줄 삭제", description = "고정 근무 규칙을 삭제합니다. 이미 생성된 실제 근무는 보존하고 이후 배치 생성에서 제외합니다.")
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

	@Operation(summary = "매니저 - 근무자 고정 스케줄 목록 조회", description = "업장의 활성화된 모든 고정 스케줄 목록을 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "고정 스케줄 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "404 Error 실패 케이스",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "존재하지 않는 업장입니다.",
						value = "{\"code\" : \"B019\"}"
					),
				})),
	})
	ResponseEntity<CommonApiResponse<List<FixedWorkerScheduleResponseDto>>> getWorkerScheduleList(
		@PathVariable Long workspaceId
	);
}
