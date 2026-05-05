package com.dreamteam.alter.domain.notification.port.inbound;

import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;

public interface GetNotificationConsentUseCase {
    GetNotificationConsentResult execute(GetNotificationConsentCommand command);
}
