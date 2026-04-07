package com.dreamteam.alter.domain.admin.port.outbound;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardResponseDto;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;

import java.util.Optional;

public interface AdminDashboardCacheRepository {

    Optional<AdminDashboardResponseDto> find(DashboardPeriod period, int year);

    void save(DashboardPeriod period, int year, AdminDashboardResponseDto data);

}
