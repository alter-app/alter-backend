package com.dreamteam.alter.domain.workspace.command;

import com.dreamteam.alter.domain.user.entity.User;

public record GetReceivedSubstituteRequestDetailCommand(User user, Long requestId) {
}
