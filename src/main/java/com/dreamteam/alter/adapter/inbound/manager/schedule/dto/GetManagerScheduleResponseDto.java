package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 스케줄 목록 조회 통합 응답")
public class GetManagerScheduleResponseDto {

    @Schema(description = "전체 근무자 총 근무 시간", example = "80.0")
    private double totalWorkHours;

    @Schema(description = "예상 인건비 (최저시급 기준)", example = "825600")
    private Long estimatedLaborCost;

    @Schema(description = "스케줄 목록")
    private List<ManagerScheduleResponseDto> schedules;

    public static GetManagerScheduleResponseDto of(
        double totalWorkHours,
        Long estimatedLaborCost,
        List<ManagerScheduleResponseDto> schedules
    ) {
        return GetManagerScheduleResponseDto.builder()
            .totalWorkHours(totalWorkHours)
            .estimatedLaborCost(estimatedLaborCost)
            .schedules(schedules)
            .build();
    }
}
