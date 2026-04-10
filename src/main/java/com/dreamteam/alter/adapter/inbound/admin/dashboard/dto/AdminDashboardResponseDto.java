package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardStatistics;
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
@Schema(description = "관리자 대시보드 통계 응답 DTO")
public class AdminDashboardResponseDto {

    @Schema(description = "업장 등록 현황 차트 데이터")
    private AdminDashboardChartDataDto workspaceChart;

    @Schema(description = "회원 가입 현황 차트 데이터")
    private AdminDashboardChartDataDto memberChart;

    @Schema(description = "주간 신고 수", example = "42")
    private Long weeklyReportCount;

    @Schema(description = "주간 신규 합류자 수", example = "128")
    private Long weeklyNewWorkerCount;

    public static AdminDashboardResponseDto from(DashboardStatistics statistics) {
        return AdminDashboardResponseDto.builder()
            .workspaceChart(AdminDashboardChartDataDto.from(statistics.getWorkspaceChart()))
            .memberChart(AdminDashboardChartDataDto.from(statistics.getMemberChart()))
            .weeklyReportCount(statistics.getWeeklyReportCount())
            .weeklyNewWorkerCount(statistics.getWeeklyNewWorkerCount())
            .build();
    }
}
