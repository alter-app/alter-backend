package com.dreamteam.alter.domain.admin.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DashboardChartData {

    private DashboardPeriod period;
    private int year;
    private double yearOverYearGrowthRate;
    private List<DashboardDataPoint> dataPoints;

    public static DashboardChartData of(
        DashboardPeriod period,
        int year,
        double yearOverYearGrowthRate,
        List<DashboardDataPoint> dataPoints
    ) {
        return DashboardChartData.builder()
            .period(period)
            .year(year)
            .yearOverYearGrowthRate(yearOverYearGrowthRate)
            .dataPoints(dataPoints)
            .build();
    }
}
