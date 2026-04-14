package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.domain.admin.type.DashboardChartStatistics;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetDashboardChartUseCase {
    DashboardChartStatistics execute(AdminActor actor, DashboardPeriod period, Integer year);
}
