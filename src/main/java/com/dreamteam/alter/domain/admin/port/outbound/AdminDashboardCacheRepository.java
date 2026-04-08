package com.dreamteam.alter.domain.admin.port.outbound;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.admin.type.DashboardStatistics;

import java.util.Optional;

public interface AdminDashboardCacheRepository {

    Optional<DashboardStatistics> find(DashboardPeriod period, int year);

    void save(DashboardPeriod period, int year, DashboardStatistics data);

}
