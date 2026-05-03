package com.dreamteam.alter.domain.user.command;

import com.dreamteam.alter.domain.user.entity.User;

public record UpdatePasswordCommand(User user, String currentPassword, String newPassword) {
}
