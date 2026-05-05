package com.dreamteam.alter.domain.notification.command;

import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
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
    private NotificationConsentType type;
    private boolean consent;

    public static UpdateNotificationConsentCommand of(User user, NotificationConsentType type, boolean consent) {
        return UpdateNotificationConsentCommand.builder()
            .user(user)
            .type(type)
            .consent(consent)
            .build();
    }
}
