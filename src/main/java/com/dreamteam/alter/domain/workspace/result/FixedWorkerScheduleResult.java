package com.dreamteam.alter.domain.workspace.result;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;

public record FixedWorkerScheduleResult(
    Long id,
    Long workspaceWorkerId,
    DayOfWeek startDayOfWeek,
    LocalTime startTime,
    DayOfWeek endDayOfWeek,
    LocalTime endTime,
    WorkspaceWorkerScheduleStatus status,
    WorkspaceWorkerScheduleRecurrenceType recurrenceType,
    Integer weekOfMonth,
    LocalDate startDate,
    LocalDate endDate
) {
    public static FixedWorkerScheduleResult from(WorkspaceWorkerSchedule schedule) {
        return new FixedWorkerScheduleResult(
            schedule.getId(), schedule.getWorkspaceWorker().getId(),
            schedule.getStartDayOfWeek(), schedule.getStartTime(),
            schedule.getEndDayOfWeek(), schedule.getEndTime(), schedule.getStatus(),
            schedule.getRecurrenceType(), schedule.getWeekOfMonth(),
            schedule.getStartDate(), schedule.getEndDate()
        );
    }
}
