package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.user.context.AppActor;

public interface MarkNotificationsAsReadUseCase {
    void execute(AppActor actor, Long notificationId);
}
