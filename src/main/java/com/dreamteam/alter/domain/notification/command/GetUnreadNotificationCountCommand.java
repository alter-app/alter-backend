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
public class GetUnreadNotificationCountCommand {

    private final User targetUser;
    private final TokenScope scope;

    public static GetUnreadNotificationCountCommand of(AppActor actor) {
        return new GetUnreadNotificationCountCommand(actor.getUser(), TokenScope.APP);
    }

    public static GetUnreadNotificationCountCommand of(ManagerActor actor) {
        return new GetUnreadNotificationCountCommand(actor.getManagerUser().getUser(), TokenScope.MANAGER);
    }
}
