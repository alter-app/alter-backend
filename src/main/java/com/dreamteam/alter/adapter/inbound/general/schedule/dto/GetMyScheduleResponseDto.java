package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.domain.workspace.result.WorkspaceWorkSummaryResult;
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

    @Schema(description = "총 근무 시간 (월 조회: 해당 월, 일 조회: 해당 일, 인자 없는 조회: 이번 주)", example = "40.5")
    private double totalWorkHours;

    @Schema(description = "예상 급여 (최저시급 기준, 업장별 예상 급여의 합. 월 조회 시에만 제공)", example = "412800")
    private Long estimatedSalary;

    @Schema(description = "업장별 근무시간·예상 급여 (월 조회 시에만 제공, 워크스페이스 ID 오름차순)")
    private List<MyWorkspaceWorkSummaryDto> workspaceSummaries;

    @Schema(description = "스케줄 목록")
    private List<MyScheduleResponseDto> schedules;

    // 일·주 조회: 근무시간만 제공
    public static GetMyScheduleResponseDto of(double totalWorkHours, List<MyScheduleResponseDto> schedules) {
        return GetMyScheduleResponseDto.builder()
            .totalWorkHours(totalWorkHours)
            .schedules(schedules)
            .build();
    }

    // 월 조회: 예상 급여와 업장별 내역까지 제공
    public static GetMyScheduleResponseDto of(
        double totalWorkHours,
        long estimatedSalary,
        List<WorkspaceWorkSummaryResult> workspaceSummaries,
        List<MyScheduleResponseDto> schedules
    ) {
        return GetMyScheduleResponseDto.builder()
            .totalWorkHours(totalWorkHours)
            .estimatedSalary(estimatedSalary)
            .workspaceSummaries(workspaceSummaries.stream().map(MyWorkspaceWorkSummaryDto::from).toList())
            .schedules(schedules)
            .build();
    }
}
