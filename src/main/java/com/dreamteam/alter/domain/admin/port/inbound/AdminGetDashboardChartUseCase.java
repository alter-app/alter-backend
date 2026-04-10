package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.domain.admin.type.DashboardChartStatistics;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;

public interface AdminGetDashboardChartUseCase {
    DashboardChartStatistics execute(DashboardPeriod period, Integer year);
}
