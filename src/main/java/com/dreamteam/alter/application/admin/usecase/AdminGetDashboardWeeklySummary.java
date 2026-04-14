package com.dreamteam.alter.application.admin.usecase;

import com.dreamteam.alter.domain.admin.port.inbound.AdminGetDashboardWeeklySummaryUseCase;
import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository;
import com.dreamteam.alter.domain.admin.type.DashboardWeeklySummary;
import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service("adminGetDashboardWeeklySummary")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetDashboardWeeklySummary implements AdminGetDashboardWeeklySummaryUseCase {

    private final AdminDashboardQueryRepository adminDashboardQueryRepository;

    @Override
    public DashboardWeeklySummary execute(AdminActor actor) {
        // 주간 범위 [이번 주 월요일 00:00, 다음 주 월요일 00:00) 반개구간
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusWeeks(1).atStartOfDay();

        long weeklyReportCount = adminDashboardQueryRepository.countReportsBetween(weekStart, weekEnd);
        long weeklyNewWorkerCount = adminDashboardQueryRepository.countNewWorkersBetween(weekStart, weekEnd);

        return DashboardWeeklySummary.of(weeklyReportCount, weeklyNewWorkerCount);
    }
}
