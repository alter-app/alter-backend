package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.WorkerScheduleDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 - 근무자 고정 스케줄 등록 DTO")
public class CreateWorkerScheduleRequestDto {

	@NotNull(message = "업장 근무자 ID는 필수입니다.")
	@Schema(description = "업장 근무자 ID", example = "1")
	private Long workspaceWorkerId;

	@NotEmpty(message = "스케줄 목록은 필수입니다.")
	@Valid
	private List<WorkerScheduleDto> schedules;
}
