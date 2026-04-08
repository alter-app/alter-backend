package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardChartData;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.admin.type.DashboardStatistics;
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
@Schema(description = "관리자 대시보드 통계 응답 DTO")
public class AdminDashboardResponseDto {

    @Schema(description = "업장 등록 현황 차트 데이터")
    private ChartData workspaceChart;

    @Schema(description = "회원 가입 현황 차트 데이터")
    private ChartData memberChart;

    @Schema(description = "주간 신고 수", example = "42")
    private Long weeklyReportCount;

    @Schema(description = "주간 신규 합류자 수", example = "128")
    private Long weeklyNewWorkerCount;

    public static AdminDashboardResponseDto from(DashboardStatistics statistics) {
        return AdminDashboardResponseDto.builder()
            .workspaceChart(ChartData.from(statistics.getWorkspaceChart()))
            .memberChart(ChartData.from(statistics.getMemberChart()))
            .weeklyReportCount(statistics.getWeeklyReportCount())
            .weeklyNewWorkerCount(statistics.getWeeklyNewWorkerCount())
            .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @Schema(description = "차트 데이터")
    public static class ChartData {

        @Schema(description = "조회 기간 단위", example = "MONTHLY")
        private DashboardPeriod period;

        @Schema(description = "조회 연도", example = "2024")
        private int year;

        @Schema(description = "전년 대비 증감률 (%)", example = "12.5")
        private Double yearOverYearGrowthRate;

        @Schema(description = "기간별 데이터 포인트 목록")
        private List<DataPoint> dataPoints;

        public static ChartData from(DashboardChartData chartData) {
            return ChartData.builder()
                .period(chartData.getPeriod())
                .year(chartData.getYear())
                .yearOverYearGrowthRate(chartData.getYearOverYearGrowthRate())
                .dataPoints(chartData.getDataPoints().stream()
                    .map(dp -> DataPoint.of(dp.getLabel(), dp.getCount()))
                    .toList())
                .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @Schema(description = "데이터 포인트")
    public static class DataPoint {

        @Schema(description = "레이블 (예: '1월', '1주', '2024')", example = "1월")
        private String label;

        @Schema(description = "해당 기간의 집계 수", example = "15")
        private long count;

        public static DataPoint of(String label, long count) {
            return DataPoint.builder()
                .label(label)
                .count(count)
                .build();
        }
    }
}
