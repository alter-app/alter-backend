package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.notification.dto.UnreadNotificationCountResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetUnreadNotificationCountUseCase {
    UnreadNotificationCountResponseDto execute(ManagerActor actor);
}
