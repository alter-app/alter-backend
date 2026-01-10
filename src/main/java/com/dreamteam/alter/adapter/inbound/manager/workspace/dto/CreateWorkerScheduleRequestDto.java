package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 - 근무자 고정 스케줄 등록 DTO")
public class CreateWorkerScheduleRequestDto {

	@NotNull(message = "요일은 필수입니다")
	@Schema(description = "요일", example = "MONDAY")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "시작 시간은 필수입니다")
	@Schema(description = "시작 시간", example = "09:00:00")
	private LocalTime startTime;

	@NotNull(message = "종료 시간은 필수입니다")
	@Schema(description = "종료 시간", example = "18:00:00")
	private LocalTime endTime;
}
