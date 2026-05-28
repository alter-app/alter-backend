package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetUnreadNotificationCountUseCase {
    long execute(ManagerActor actor);
}
