package com.dreamteam.alter.domain.admin.port.outbound;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminDashboardQueryRepository {

    List<PeriodCount> countWorkspacesByPeriod(DashboardPeriod period, int year);

    List<PeriodCount> countUsersByPeriod(DashboardPeriod period, int year);

    long countReportsBetween(LocalDateTime from, LocalDateTime to);

    long countActiveUsersBetween(LocalDateTime from, LocalDateTime to);

    long countWorkspacesInYear(int year);

    long countUsersInYear(int year);

    record PeriodCount(String label, long count) {}
}
