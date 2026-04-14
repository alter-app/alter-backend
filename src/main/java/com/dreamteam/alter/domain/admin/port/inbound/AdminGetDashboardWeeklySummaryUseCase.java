package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.domain.admin.type.DashboardWeeklySummary;
import com.dreamteam.alter.domain.user.context.AdminActor;

public interface AdminGetDashboardWeeklySummaryUseCase {
    DashboardWeeklySummary execute(AdminActor actor);
}
