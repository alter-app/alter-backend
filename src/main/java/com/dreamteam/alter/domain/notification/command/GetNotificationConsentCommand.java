package com.dreamteam.alter.domain.notification.command;

import com.dreamteam.alter.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetNotificationConsentCommand {

    private User user;

    public static GetNotificationConsentCommand from(User user) {
        return GetNotificationConsentCommand.builder()
            .user(user)
            .build();
    }
}
