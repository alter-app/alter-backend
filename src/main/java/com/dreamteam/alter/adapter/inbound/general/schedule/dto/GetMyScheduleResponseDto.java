package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스케줄 조회 통합 응답")
public class GetMyScheduleResponseDto {

    @Schema(description = "총 근무 시간", example = "40.5")
    private double totalWorkHours;

    @Schema(description = "스케줄 목록")
    private List<MyScheduleResponseDto> schedules;

    public static GetMyScheduleResponseDto of(double totalWorkHours, List<MyScheduleResponseDto> schedules) {
        return GetMyScheduleResponseDto.builder()
                .totalWorkHours(totalWorkHours)
                .schedules(schedules)
                .build();
    }
}
