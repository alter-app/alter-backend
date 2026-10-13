package com.dreamteam.alter.domain.workspace.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

public record FixedWorkerScheduleCommand(
    DayOfWeek startDayOfWeek,
    LocalTime startTime,
    DayOfWeek endDayOfWeek,
    LocalTime endTime,
    WorkspaceWorkerScheduleRecurrenceType recurrenceType,
    Integer weekOfMonth,
    LocalDate startDate,
    LocalDate endDate
) {}
