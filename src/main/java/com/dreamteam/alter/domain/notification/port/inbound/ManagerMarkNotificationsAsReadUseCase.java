package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.MarkNotificationsAsReadRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerMarkNotificationsAsReadUseCase {
    void execute(ManagerActor actor, MarkNotificationsAsReadRequestDto request);
}
