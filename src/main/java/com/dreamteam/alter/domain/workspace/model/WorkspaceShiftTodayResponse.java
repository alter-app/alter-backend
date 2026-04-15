package com.dreamteam.alter.domain.workspace.model;

import java.time.LocalDateTime;

public record WorkspaceShiftTodayResponse(
    Long shiftId,
    String workerName,
    String profileImageUrl,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime
) {}
