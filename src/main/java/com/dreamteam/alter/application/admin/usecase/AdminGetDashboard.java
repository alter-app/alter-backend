package com.dreamteam.alter.application.admin.usecase;

import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardUseCase;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardCacheRepository;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository.PeriodCount;
import com.dreamteam.alter.domain.admin.type.DashboardChartData;
import com.dreamteam.alter.domain.admin.type.DashboardDataPoint;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.admin.type.DashboardStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service("adminGetDashboard")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetDashboard implements AdminGetDashboardUseCase {

    private final AdminDashboardQueryRepository adminDashboardQueryRepository;
    private final AdminDashboardCacheRepository adminDashboardCacheRepository;

    @Override
    public DashboardStatistics execute(DashboardPeriod period, Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();

        // 캐시 조회
        Optional<DashboardStatistics> cached = adminDashboardCacheRepository.find(period, resolvedYear);
        if (cached.isPresent()) return cached.get();

        // 차트 데이터
        List<PeriodCount> workspaceCounts = adminDashboardQueryRepository.countWorkspacesByPeriod(period, resolvedYear);
        List<PeriodCount> userCounts = adminDashboardQueryRepository.countUsersByPeriod(period, resolvedYear);

        // 전년 대비 증감률
        double workspaceGrowthRate = calcGrowthRate(
            adminDashboardQueryRepository.countWorkspacesInYear(resolvedYear),
            adminDashboardQueryRepository.countWorkspacesInYear(resolvedYear - 1)
        );
        double userGrowthRate = calcGrowthRate(
            adminDashboardQueryRepository.countUsersInYear(resolvedYear),
            adminDashboardQueryRepository.countUsersInYear(resolvedYear - 1)
        );

        // 주간 범위 (이번 주 월요일 00:00 ~ 일요일 23:59:59)
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);

        long weeklyReportCount = adminDashboardQueryRepository.countReportsBetween(weekStart, weekEnd);
        long weeklyNewWorkerCount = adminDashboardQueryRepository.countNewWorkersBetween(weekStart, weekEnd);

        // 도메인 모델 조립
        DashboardChartData workspaceChart = DashboardChartData.of(
            period, resolvedYear, workspaceGrowthRate, toDataPoints(workspaceCounts)
        );
        DashboardChartData memberChart = DashboardChartData.of(
            period, resolvedYear, userGrowthRate, toDataPoints(userCounts)
        );

        DashboardStatistics result = DashboardStatistics.of(
            workspaceChart, memberChart, weeklyReportCount, weeklyNewWorkerCount
        );

        // 캐시 저장
        adminDashboardCacheRepository.save(period, resolvedYear, result);

        return result;
    }

    private double calcGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double rate = (double) (current - previous) / previous * 100;
        return Math.round(rate * 10.0) / 10.0;
    }

    private List<DashboardDataPoint> toDataPoints(List<PeriodCount> counts) {
        return counts.stream()
            .map(pc -> DashboardDataPoint.of(pc.label(), pc.count()))
            .toList();
    }
}
