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
public class DashboardChartStatistics {

    private DashboardChartData workspaceChart;
    private DashboardChartData memberChart;

    public static DashboardChartStatistics of(DashboardChartData workspaceChart, DashboardChartData memberChart) {
        return DashboardChartStatistics.builder()
            .workspaceChart(workspaceChart)
            .memberChart(memberChart)
            .build();
    }
}
