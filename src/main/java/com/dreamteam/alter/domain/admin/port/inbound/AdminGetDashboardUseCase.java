package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.admin.type.DashboardStatistics;

public interface AdminGetDashboardUseCase {
    DashboardStatistics execute(DashboardPeriod period, Integer year);
}
