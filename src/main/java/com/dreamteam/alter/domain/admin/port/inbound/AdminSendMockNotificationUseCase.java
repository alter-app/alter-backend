package com.dreamteam.alter.domain.admin.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.notification.dto.AdminSendMockNotificationRequestDto;

public interface AdminSendMockNotificationUseCase {
    void execute(AdminSendMockNotificationRequestDto request);
}
