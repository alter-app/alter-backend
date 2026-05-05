package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;

public interface UpdateNotificationConsentUseCase {
    void execute(UpdateNotificationConsentCommand command);
}
