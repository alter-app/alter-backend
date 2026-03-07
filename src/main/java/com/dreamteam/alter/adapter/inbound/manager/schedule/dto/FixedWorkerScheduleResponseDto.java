package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
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

	public static FixedWorkerScheduleResponseDto of(WorkspaceWorkerSchedule s) {
		return new FixedWorkerScheduleResponseDto(
			s.getId(),
			s.getWorkspaceWorker().getId(),
			s.getStartDayOfWeek(),
			s.getStartTime(),
			s.getEndDayOfWeek(),
			s.getEndTime(),
			s.getStatus()
		);
	}
}
