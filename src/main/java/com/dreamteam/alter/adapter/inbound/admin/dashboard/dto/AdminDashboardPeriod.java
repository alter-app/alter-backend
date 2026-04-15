package com.dreamteam.alter.adapter.inbound.admin.dashboard.dto;

import com.dreamteam.alter.domain.admin.type.DashboardPeriod;

public enum AdminDashboardPeriod {
    WEEKLY, MONTHLY, YEARLY;

    public DashboardPeriod toDomain() {
        return DashboardPeriod.valueOf(this.name());
    }

    public static AdminDashboardPeriod from(DashboardPeriod period) {
        return AdminDashboardPeriod.valueOf(period.name());
    }
}
