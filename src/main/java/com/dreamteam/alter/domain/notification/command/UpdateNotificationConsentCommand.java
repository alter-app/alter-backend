package com.dreamteam.alter.domain.notification.command;

import com.dreamteam.alter.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNotificationConsentCommand {

    private User user;
    private boolean notificationConsent;
    private boolean nightNotificationConsent;

    public static UpdateNotificationConsentCommand of(User user, boolean notificationConsent, boolean nightNotificationConsent) {
        return UpdateNotificationConsentCommand.builder()
            .user(user)
            .notificationConsent(notificationConsent)
            .nightNotificationConsent(nightNotificationConsent)
            .build();
    }
}
