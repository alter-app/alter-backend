package com.dreamteam.alter.domain.workspace.model;

import java.time.LocalDateTime;

public record WorkspaceShiftTodayResponse(
    Long shiftId,
	Long workerId,
    String workerName,
    String profileImageUrl,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime
) {}
