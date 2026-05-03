package com.dreamteam.alter.domain.user.command;

import com.dreamteam.alter.domain.user.entity.User;

public record UpdateEmailCommand(User user, String sessionId) {
}
