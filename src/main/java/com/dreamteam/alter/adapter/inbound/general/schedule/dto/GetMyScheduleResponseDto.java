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
@Schema(description = "스케줄 조회 통합 응답")
public class GetMyScheduleResponseDto {

    @Schema(description = "총 근무 시간 (월·일 조회 시 해당 월 기준, 인자 없는 조회 시 이번 주 기준)", example = "40.5")
    private double totalWorkHours;

    @Schema(description = "예상 급여 (최저시급 기준, 업장별 예상 급여의 합. 월·일 조회 시 해당 월 기준으로 제공)", example = "412800")
    private Long estimatedSalary;

    @Schema(description = "업장별 근무시간·예상 급여 (월·일 조회 시 해당 월 기준으로 제공)")
    private List<MyWorkspaceWorkSummaryDto> workspaceSummaries;

    @Schema(description = "스케줄 목록")
    private List<MyScheduleResponseDto> schedules;

    public static GetMyScheduleResponseDto of(
        double totalWorkHours,
        Long estimatedSalary,
        List<MyWorkspaceWorkSummaryDto> workspaceSummaries,
        List<MyScheduleResponseDto> schedules
    ) {
        return GetMyScheduleResponseDto.builder()
            .totalWorkHours(totalWorkHours)
            .estimatedSalary(estimatedSalary)
            .workspaceSummaries(workspaceSummaries)
            .schedules(schedules)
            .build();
    }
}
