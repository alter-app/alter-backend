package com.dreamteam.alter.domain.posting.command;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record UpdatePostingScheduleCommand(
    Long id,
    List<DayOfWeek> workingDays,
    LocalTime startTime,
    LocalTime endTime,
    int positionsNeeded,
    String position
) {}
