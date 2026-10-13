package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.workspace.result.FixedWorkerScheduleResult;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "고정 근무 스케줄 응답")
public class FixedWorkerScheduleResponseDto {
	@Schema(description = "고정 스케줄 ID", example = "1")
	private Long id;

	@Schema(description = "업장 근무자 ID", example = "10")
	private Long workspaceWorkerId;

	@Schema(description = "시작 요일", example = "MONDAY")
	private DayOfWeek startDayOfWeek;

	@Schema(description = "시작 시간", example = "09:00:00")
	private LocalTime startTime;

	@Schema(description = "종료 요일", example = "MONDAY")
	private DayOfWeek endDayOfWeek;

	@Schema(description = "종료 시간", example = "18:00:00")
	private LocalTime endTime;

	@Schema(description = "스케줄 상태", example = "ACTIVATED")
	private WorkspaceWorkerScheduleStatus status;

	@Schema(description = "반복 방식")
	private DescribedEnumDto<WorkspaceWorkerScheduleRecurrenceType> recurrenceType;

	@Schema(description = "매월 같은 요일의 발생 순서(1~5). 매주는 null", example = "2")
	private Integer weekOfMonth;

	@Schema(description = "적용 시작일(포함). 기존 무기한 매주 규칙은 null", example = "2026-10-01")
	private LocalDate startDate;

	@Schema(description = "적용 종료일(포함). null이면 무기한", example = "2027-03-31")
	private LocalDate endDate;

	public static FixedWorkerScheduleResponseDto from(FixedWorkerScheduleResult s) {
		return new FixedWorkerScheduleResponseDto(
			s.id(), s.workspaceWorkerId(), s.startDayOfWeek(), s.startTime(),
			s.endDayOfWeek(), s.endTime(), s.status(),
			DescribedEnumDto.of(s.recurrenceType(), WorkspaceWorkerScheduleRecurrenceType.describe()),
			s.weekOfMonth(), s.startDate(), s.endDate()
		);
	}
}
