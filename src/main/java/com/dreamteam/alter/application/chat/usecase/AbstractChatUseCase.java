package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;

public abstract class AbstractChatUseCase {

    protected TokenScope getParticipantScope(AppActor actor) {
        return TokenScope.APP;
    }

    protected TokenScope getParticipantScope(ManagerActor actor) {
        return TokenScope.MANAGER;
    }

    protected Long getParticipantId(AppActor actor) {
        return actor.getUserId();
    }

    protected Long getParticipantId(ManagerActor actor) {
        return actor.getUserId();
    }

    protected TokenScope getParticipantScope(User user) {
        return TokenScope.APP;
    }

    protected TokenScope getParticipantScope(ManagerUser managerUser) {
        return TokenScope.MANAGER;
    }

    protected Long getParticipantId(User user) {
        return user.getId();
    }

    protected Long getParticipantId(ManagerUser managerUser) {
        return managerUser.getUser().getId();
    }

}
