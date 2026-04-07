package com.dreamteam.alter.application.admin.usecase;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardResponseDto;
import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardUseCase;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository.PeriodCount;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service("adminGetDashboard")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetDashboard implements AdminGetDashboardUseCase {

    private final AdminDashboardQueryRepository adminDashboardQueryRepository;

    @Override
    public AdminDashboardResponseDto execute(AdminDashboardRequestDto request) {
        int year = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
        DashboardPeriod period = request.getPeriod();

        // 차트 데이터
        List<PeriodCount> workspaceCounts = adminDashboardQueryRepository.countWorkspacesByPeriod(period, year);
        List<PeriodCount> userCounts = adminDashboardQueryRepository.countUsersByPeriod(period, year);

        // 전년 대비 증감률
        double workspaceGrowthRate = calcGrowthRate(
            adminDashboardQueryRepository.countWorkspacesInYear(year),
            adminDashboardQueryRepository.countWorkspacesInYear(year - 1)
        );
        double userGrowthRate = calcGrowthRate(
            adminDashboardQueryRepository.countUsersInYear(year),
            adminDashboardQueryRepository.countUsersInYear(year - 1)
        );

        // 주간 범위 (이번 주 월요일 00:00 ~ 일요일 23:59:59)
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);

        long weeklyReportCount = adminDashboardQueryRepository.countReportsBetween(weekStart, weekEnd);
        long weeklyActiveUserCount = adminDashboardQueryRepository.countActiveUsersBetween(weekStart, weekEnd);

        // 응답 조립
        AdminDashboardResponseDto.ChartData workspaceChart = AdminDashboardResponseDto.ChartData.of(
            period,
            year,
            workspaceGrowthRate,
            toDataPoints(workspaceCounts)
        );

        AdminDashboardResponseDto.ChartData memberChart = AdminDashboardResponseDto.ChartData.of(
            period,
            year,
            userGrowthRate,
            toDataPoints(userCounts)
        );

        return AdminDashboardResponseDto.of(workspaceChart, memberChart, weeklyReportCount, weeklyActiveUserCount);
    }

    private double calcGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double rate = (double) (current - previous) / previous * 100;
        return Math.round(rate * 10.0) / 10.0;
    }

    private List<AdminDashboardResponseDto.DataPoint> toDataPoints(List<PeriodCount> counts) {
        return counts.stream()
            .map(pc -> AdminDashboardResponseDto.DataPoint.of(pc.label(), pc.count()))
            .toList();
    }
}
