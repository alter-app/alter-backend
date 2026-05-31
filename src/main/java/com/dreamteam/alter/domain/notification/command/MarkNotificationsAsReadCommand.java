package com.dreamteam.alter.domain.notification.command;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MarkNotificationsAsReadCommand {

    private final User targetUser;
    private final TokenScope scope;
    private final Long notificationId;

    public static MarkNotificationsAsReadCommand of(AppActor actor, Long notificationId) {
        return new MarkNotificationsAsReadCommand(actor.getUser(), TokenScope.APP, notificationId);
    }

    public static MarkNotificationsAsReadCommand of(ManagerActor actor, Long notificationId) {
        return new MarkNotificationsAsReadCommand(actor.getManagerUser().getUser(), TokenScope.MANAGER, notificationId);
    }
}
