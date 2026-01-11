package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "내 월별 스케쥴 응답")
public class MyScheduleMonthlyResponseDto {

    @Schema(description = "월 총 근무시간", example = "160.5")
    private double totalWorkHoursInMonth;

    @Schema(description = "월별 스케쥴 목록")
    private List<MyScheduleResponseDto> schedules;
}
