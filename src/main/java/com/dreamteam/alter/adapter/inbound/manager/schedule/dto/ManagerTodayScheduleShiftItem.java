package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "근무 항목")
public class ManagerTodayScheduleShiftItem {

	@Schema(description = "스케줄 ID", example = "1")
	private Long shiftId;

	@Schema(description = "근무 시작 시간", example = "2024-01-15T09:00:00")
	private LocalDateTime startDateTime;

	@Schema(description = "근무 종료 시간", example = "2024-01-15T18:00:00")
	private LocalDateTime endDateTime;

	public static ManagerTodayScheduleShiftItem of(Long shiftId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		return ManagerTodayScheduleShiftItem.builder()
			.shiftId(shiftId)
			.startDateTime(startDateTime)
			.endDateTime(endDateTime)
			.build();
	}
}
