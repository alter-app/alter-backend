package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardChartData;
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
@Schema(description = "차트 데이터")
public class AdminDashboardChartDataDto {

    @Schema(description = "조회 기간 단위", example = "MONTHLY")
    private AdminDashboardPeriod period;

    @Schema(description = "조회 연도", example = "2024")
    private int year;

    @Schema(description = "전년 대비 증감률 (%)", example = "12.5")
    private Double yearOverYearGrowthRate;

    @Schema(description = "기간별 데이터 포인트 목록")
    private List<AdminDashboardDataPointDto> dataPoints;

    public static AdminDashboardChartDataDto from(DashboardChartData chartData) {
        return AdminDashboardChartDataDto.builder()
            .period(AdminDashboardPeriod.from(chartData.getPeriod()))
            .year(chartData.getYear())
            .yearOverYearGrowthRate(chartData.getYearOverYearGrowthRate())
            .dataPoints(chartData.getDataPoints().stream()
                .map(dp -> AdminDashboardDataPointDto.of(dp.getLabel(), dp.getCount()))
                .toList())
            .build();
    }
}
