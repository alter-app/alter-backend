package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.dashboard.dto.AdminDashboardResponseDto;

public interface AdminGetDashboardUseCase {
    AdminDashboardResponseDto execute(AdminDashboardRequestDto request);
}
