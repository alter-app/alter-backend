package com.dreamteam.alter.domain.admin.port.outbound;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminDashboardQueryRepository {

    List<PeriodCount> countWorkspacesByPeriod(DashboardPeriod period, int year);

    List<PeriodCount> countUsersByPeriod(DashboardPeriod period, int year);

    /** [from, to) 반개구간으로 조회 */
    long countReportsBetween(LocalDateTime from, LocalDateTime to);

    /** [from, to) 반개구간으로 조회 */
    long countNewWorkersBetween(LocalDateTime from, LocalDateTime to);

    long countWorkspacesInYear(int year);

    long countUsersInYear(int year);

    record PeriodCount(String label, long count) {}
}
