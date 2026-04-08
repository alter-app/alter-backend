package com.dreamteam.alter.domain.admin.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DashboardStatistics {

    private DashboardChartData workspaceChart;
    private DashboardChartData memberChart;
    private long weeklyReportCount;
    private long weeklyNewWorkerCount;

    public static DashboardStatistics of(
        DashboardChartData workspaceChart,
        DashboardChartData memberChart,
        long weeklyReportCount,
        long weeklyNewWorkerCount
    ) {
        return DashboardStatistics.builder()
            .workspaceChart(workspaceChart)
            .memberChart(memberChart)
            .weeklyReportCount(weeklyReportCount)
            .weeklyNewWorkerCount(weeklyNewWorkerCount)
            .build();
    }
}
