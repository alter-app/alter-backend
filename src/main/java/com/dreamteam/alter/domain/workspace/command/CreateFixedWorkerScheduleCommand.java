package com.dreamteam.alter.domain.workspace.command;

import java.util.List;

public record CreateFixedWorkerScheduleCommand(
    Long workspaceWorkerId,
    List<FixedWorkerScheduleCommand> schedules
) {}
