package com.dreamteam.alter.domain.workspace.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

public class FixedWorkerScheduleCalculator {

    public static List<Occurrence> occurrences(WorkspaceWorkerSchedule schedule, LocalDate from, LocalDate to) {
        LocalDate first = schedule.getStartDate() != null && schedule.getStartDate().isAfter(from)
            ? schedule.getStartDate() : from;
        LocalDate last = schedule.getEndDate() != null && schedule.getEndDate().isBefore(to)
            ? schedule.getEndDate() : to;
        List<Occurrence> result = new ArrayList<>();
        if (first.isAfter(last)) {
            return result;
        }

        if (schedule.getRecurrenceType() == WorkspaceWorkerScheduleRecurrenceType.MONTHLY) {
            for (YearMonth month = YearMonth.from(first); !month.isAfter(YearMonth.from(last)); month = month.plusMonths(1)) {
                LocalDate date = month.atDay(1)
                    .with(TemporalAdjusters.nextOrSame(schedule.getStartDayOfWeek()))
                    .plusWeeks(schedule.getWeekOfMonth() - 1);
                if (YearMonth.from(date).equals(month) && !date.isBefore(first) && !date.isAfter(last)) {
                    result.add(occurrence(schedule, date));
                }
            }
        } else {
            for (LocalDate date = first.with(TemporalAdjusters.nextOrSame(schedule.getStartDayOfWeek()));
                 !date.isAfter(last); date = date.plusWeeks(1)) {
                result.add(occurrence(schedule, date));
            }
        }
        return result;
    }

    public static boolean overlaps(WorkspaceWorkerSchedule left, WorkspaceWorkerSchedule right) {
        LocalDate lower = later(left.getStartDate(), right.getStartDate());
        LocalDate upper = earlier(left.getEndDate(), right.getEndDate());
        // 무기한 요일 규칙도 Gregorian 달력의 400년 주기로 반복된다.
        if (lower == null) {
            lower = upper == null ? LocalDate.of(2000, 1, 1) : upper.minusYears(400);
        }
        LocalDate from = lower.minusDays(6);
        LocalDate cycleEnd = lower.plusYears(400).plusDays(6);
        LocalDate to = upper == null ? cycleEnd : earlier(upper.plusDays(6), cycleEnd);
        if (from.isAfter(to)) {
            return false;
        }

        List<Occurrence> leftOccurrences = occurrences(left, from, to);
        List<Occurrence> rightOccurrences = occurrences(right, from, to);
        int i = 0;
        int j = 0;
        while (i < leftOccurrences.size() && j < rightOccurrences.size()) {
            Occurrence a = leftOccurrences.get(i);
            Occurrence b = rightOccurrences.get(j);
            if (a.start().isBefore(b.end()) && b.start().isBefore(a.end())) {
                return true;
            }
            if (!a.end().isAfter(b.end())) {
                i++;
            } else {
                j++;
            }
        }
        return false;
    }

    private static Occurrence occurrence(WorkspaceWorkerSchedule schedule, LocalDate date) {
        int offset = Math.floorMod(
            schedule.getEndDayOfWeek().getValue() - schedule.getStartDayOfWeek().getValue(), 7
        );
        return new Occurrence(date.atTime(schedule.getStartTime()), date.plusDays(offset).atTime(schedule.getEndTime()));
    }

    private static LocalDate later(LocalDate left, LocalDate right) {
        return left == null ? right : right == null || left.isAfter(right) ? left : right;
    }

    private static LocalDate earlier(LocalDate left, LocalDate right) {
        return left == null ? right : right == null || left.isBefore(right) ? left : right;
    }

    public record Occurrence(LocalDateTime start, LocalDateTime end) {}
}
