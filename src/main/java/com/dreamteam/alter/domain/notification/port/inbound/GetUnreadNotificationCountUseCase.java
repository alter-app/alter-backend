package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.notification.command.GetUnreadNotificationCountCommand;

public interface GetUnreadNotificationCountUseCase {
    long execute(GetUnreadNotificationCountCommand command);
}
