package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

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
@Schema(description = "업장 스케줄 조회 통합 응답")
public class GetWorkspaceScheduleResponseDto {

    @Schema(description = "나의 총 근무 시간", example = "20.0")
    private double totalWorkHours;

    @Schema(description = "나의 예상 급여 (최저시급 기준)", example = "206400")
    private Long estimatedSalary;

    @Schema(description = "업장 전체 스케줄 목록")
    private List<WorkspaceScheduleResponseDto> schedules;

    public static GetWorkspaceScheduleResponseDto of(
        double myTotalWorkHours,
        Long estimatedSalary,
        List<WorkspaceScheduleResponseDto> schedules
    ) {
        return GetWorkspaceScheduleResponseDto.builder()
            .totalWorkHours(myTotalWorkHours)
            .estimatedSalary(estimatedSalary)
            .schedules(schedules)
            .build();
    }
}
