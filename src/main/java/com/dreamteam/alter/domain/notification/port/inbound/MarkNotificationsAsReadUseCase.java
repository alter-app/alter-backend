package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.notification.command.MarkNotificationsAsReadCommand;

public interface MarkNotificationsAsReadUseCase {
    void execute(MarkNotificationsAsReadCommand command);
}
