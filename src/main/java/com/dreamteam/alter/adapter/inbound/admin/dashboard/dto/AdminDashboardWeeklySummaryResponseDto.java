package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardWeeklySummary;
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
@Schema(description = "관리자 대시보드 주간 요약 응답 DTO")
public class AdminDashboardWeeklySummaryResponseDto {

    @Schema(description = "주간 신고 수", example = "42")
    private Long weeklyReportCount;

    @Schema(description = "주간 신규 합류자 수", example = "128")
    private Long weeklyNewWorkerCount;

    public static AdminDashboardWeeklySummaryResponseDto from(DashboardWeeklySummary summary) {
        return AdminDashboardWeeklySummaryResponseDto.builder()
            .weeklyReportCount(summary.getWeeklyReportCount())
            .weeklyNewWorkerCount(summary.getWeeklyNewWorkerCount())
            .build();
    }
}
