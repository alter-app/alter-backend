package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "근무자 스케줄 정보")
public class WorkerScheduleDto {
	@NotNull(message = "시작 요일은 필수입니다")
	@Schema(description = "시작 요일", example = "MONDAY")
	private DayOfWeek startDayOfWeek;

	@NotNull(message = "시작 시간은 필수입니다")
	@Schema(description = "시작 시간", example = "22:00:00")
	private LocalTime startTime;

	@NotNull(message = "종료 요일은 필수입니다")
	@Schema(description = "종료 요일", example = "TUESDAY")
	private DayOfWeek endDayOfWeek;

	@NotNull(message = "종료 시간은 필수입니다")
	@Schema(description = "종료 시간", example = "04:00:00")
	private LocalTime endTime;

	@Schema(description = "반복 방식. 생략 시 등록은 무기한 매주, 수정은 기존 반복과 기간 보존", example = "MONTHLY")
	private WorkspaceWorkerScheduleRecurrenceType recurrenceType;

	@Schema(description = "매월 같은 요일의 발생 순서(1~5). 없는 다섯째 요일은 건너뜀. 매주는 null", example = "2")
	private Integer weekOfMonth;

	@Schema(description = "근무 시작일 기준 적용 시작일(포함). 반복 방식 지정 시 필수", example = "2026-10-01")
	private LocalDate startDate;

	@Schema(description = "근무 시작일 기준 적용 종료일(포함). null이면 무기한", example = "2027-03-31")
	private LocalDate endDate;

	public FixedWorkerScheduleCommand toCommand() {
		return new FixedWorkerScheduleCommand(
			startDayOfWeek, startTime, endDayOfWeek, endTime,
			recurrenceType, weekOfMonth, startDate, endDate
		);
	}
}
