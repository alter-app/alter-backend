package com.dreamteam.alter.application.admin.usecase;

import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardChartUseCase;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardCacheRepository;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository.PeriodCount;
import com.dreamteam.alter.domain.admin.type.DashboardChartData;
import com.dreamteam.alter.domain.admin.type.DashboardChartStatistics;
import com.dreamteam.alter.domain.admin.type.DashboardDataPoint;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service("adminGetDashboardChart")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetDashboardChart implements AdminGetDashboardChartUseCase {

    private final AdminDashboardQueryRepository adminDashboardQueryRepository;
    private final AdminDashboardCacheRepository adminDashboardCacheRepository;

    @Override
    public DashboardChartStatistics execute(AdminActor actor, DashboardPeriod period, Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();

        // 캐시 조회
        Optional<DashboardChartStatistics> cached = adminDashboardCacheRepository.find(period, resolvedYear);
        if (cached.isPresent()) return cached.get();

        // 차트 데이터
        List<PeriodCount> workspaceCounts = adminDashboardQueryRepository.countWorkspacesByPeriod(period, resolvedYear);
        List<PeriodCount> userCounts = adminDashboardQueryRepository.countUsersByPeriod(period, resolvedYear);

        // 전년 대비 증감률 (올해 총 수는 period별 집계 합산, 전년도는 별도 쿼리)
        long workspaceCurrentTotal = workspaceCounts.stream().mapToLong(PeriodCount::count).sum();
        long userCurrentTotal = userCounts.stream().mapToLong(PeriodCount::count).sum();
        double workspaceGrowthRate = calcGrowthRate(
            workspaceCurrentTotal,
            adminDashboardQueryRepository.countWorkspacesInYear(resolvedYear - 1)
        );
        double userGrowthRate = calcGrowthRate(
            userCurrentTotal,
            adminDashboardQueryRepository.countUsersInYear(resolvedYear - 1)
        );

        // 도메인 모델 조립
        DashboardChartData workspaceChart = DashboardChartData.of(
            period, resolvedYear, workspaceGrowthRate, toDataPoints(workspaceCounts)
        );
        DashboardChartData memberChart = DashboardChartData.of(
            period, resolvedYear, userGrowthRate, toDataPoints(userCounts)
        );

        DashboardChartStatistics result = DashboardChartStatistics.of(workspaceChart, memberChart);

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
