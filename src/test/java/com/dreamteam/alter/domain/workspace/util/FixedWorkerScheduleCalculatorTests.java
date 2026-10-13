package com.dreamteam.alter.domain.workspace.util;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

class FixedWorkerScheduleCalculatorTests {

    @ParameterizedTest
    @CsvSource({"1,2026-10-06", "2,2026-10-13", "3,2026-10-20", "4,2026-10-27"})
    void monthlyUsesNthWeekday(int nth, String expected) {
        var schedule = monthly(nth, DayOfWeek.TUESDAY, "2026-10-01", null);
        var result = FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        );
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().start().toLocalDate()).isEqualTo(LocalDate.parse(expected));
    }

    @Test
    void missingFifthWeekdayIsSkipped() {
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            monthly(5, DayOfWeek.TUESDAY, "2026-10-01", null),
            LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        )).isEmpty();
    }

    @Test
    void leapDayCanBeFifthThursday() {
        var schedule = monthly(5, DayOfWeek.THURSDAY, "2024-01-01", null);
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)
        )).singleElement().satisfies(o -> assertThat(o.start().toLocalDate()).isEqualTo(LocalDate.of(2024, 2, 29)));
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28)
        )).isEmpty();
    }

    @Test
    void weeklyPeriodIncludesBothEndpoints() {
        var schedule = rule(DayOfWeek.TUESDAY, "09:00", DayOfWeek.TUESDAY, "18:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-10-13", "2026-10-20");
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        )).extracting(o -> o.start().toLocalDate())
            .containsExactly(LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 20));
    }

    @Test
    void monthlyPeriodDoesNotMoveTheRecurrenceAnchor() {
        var schedule = monthly(2, DayOfWeek.TUESDAY, "2026-10-14", "2026-12-08");
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2026, 10, 1), LocalDate.of(2027, 1, 31)
        )).extracting(o -> o.start().toLocalDate())
            .containsExactly(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 12, 8));
    }

    @Test
    void overnightShiftMayEndAfterPeriodAndMonth() {
        var schedule = rule(DayOfWeek.THURSDAY, "22:00", DayOfWeek.FRIDAY, "06:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-12-31", "2026-12-31");
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)
        )).singleElement().satisfies(o -> {
            assertThat(o.start().toString()).isEqualTo("2026-12-31T22:00");
            assertThat(o.end().toString()).isEqualTo("2027-01-01T06:00");
        });
    }

    @Test
    void sundayToMondayUsesNextWeek() {
        var schedule = rule(DayOfWeek.SUNDAY, "22:00", DayOfWeek.MONDAY, "06:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-10-25", "2026-10-25");
        assertThat(FixedWorkerScheduleCalculator.occurrences(
            schedule, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        )).singleElement().satisfies(o -> assertThat(o.end().toString()).isEqualTo("2026-10-26T06:00"));
    }

    @Test
    void distinctMonthlyOccurrencesDoNotConflict() {
        assertThat(FixedWorkerScheduleCalculator.overlaps(
            monthly(1, DayOfWeek.MONDAY, "2026-01-01", null),
            monthly(2, DayOfWeek.MONDAY, "2026-01-01", null)
        )).isFalse();
    }

    @Test
    void weeklyAndMonthlyOccurrencesConflict() {
        var weekly = rule(DayOfWeek.MONDAY, "09:00", DayOfWeek.MONDAY, "18:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-01-01", null);
        assertThat(FixedWorkerScheduleCalculator.overlaps(
            weekly, monthly(5, DayOfWeek.MONDAY, "2026-01-01", null)
        )).isTrue();
    }

    @Test
    void disjointPeriodsDoNotConflict() {
        var first = monthly(2, DayOfWeek.TUESDAY, "2026-01-01", "2026-06-30");
        var second = monthly(2, DayOfWeek.TUESDAY, "2026-07-01", null);
        assertThat(FixedWorkerScheduleCalculator.overlaps(first, second)).isFalse();
    }

    @Test
    void periodBoundaryStillDetectsOvernightConflict() {
        var first = rule(DayOfWeek.THURSDAY, "22:00", DayOfWeek.FRIDAY, "06:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-10-01", "2026-10-01");
        var second = rule(DayOfWeek.FRIDAY, "05:00", DayOfWeek.FRIDAY, "10:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-10-02", "2026-10-02");
        assertThat(FixedWorkerScheduleCalculator.overlaps(first, second)).isTrue();
        assertThat(FixedWorkerScheduleCalculator.overlaps(second, first)).isTrue();
    }

    @Test
    void touchingIntervalsAreAllowed() {
        var first = rule(DayOfWeek.SUNDAY, "22:00", DayOfWeek.MONDAY, "06:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-01-01", null);
        var second = rule(DayOfWeek.MONDAY, "06:00", DayOfWeek.MONDAY, "10:00",
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-01-01", null);
        assertThat(FixedWorkerScheduleCalculator.overlaps(first, second)).isFalse();
    }

    @Test
    void legacyRulesAlsoDetectHistoricalConflicts() {
        var legacy = WorkspaceWorkerSchedule.create(mock(WorkspaceWorker.class),
            DayOfWeek.TUESDAY, LocalTime.of(9, 0), DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        assertThat(FixedWorkerScheduleCalculator.overlaps(
            legacy, monthly(2, DayOfWeek.TUESDAY, "1800-01-01", "1800-12-31")
        )).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"MONTHLY,,2026-01-01,", "MONTHLY,0,2026-01-01,", "MONTHLY,6,2026-01-01,",
        "WEEKLY,2,2026-01-01,", "WEEKLY,,,", "MONTHLY,2,,", "WEEKLY,,2026-10-02,2026-10-01",
        ",,2026-10-01,"})
    void invalidRecurrenceIsRejected(String type, Integer nth, String start, String end) {
        assertThatThrownBy(() -> rule(DayOfWeek.MONDAY, "09:00", DayOfWeek.MONDAY, "18:00",
            type == null ? null : WorkspaceWorkerScheduleRecurrenceType.valueOf(type), nth, start, end))
            .isInstanceOf(CustomException.class)
            .satisfies(e -> assertThat(((CustomException)e).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
    }

    @ParameterizedTest
    @CsvSource({"09:00,09:00", "18:00,09:00"})
    void sameDayEmptyOrReversedTimeIsRejected(String start, String end) {
        assertThatThrownBy(() -> rule(DayOfWeek.MONDAY, start, DayOfWeek.MONDAY, end,
            WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-01-01", null))
            .isInstanceOf(CustomException.class);
    }

    private WorkspaceWorkerSchedule monthly(int nth, DayOfWeek day, String start, String end) {
        return rule(day, "09:00", day, "18:00", WorkspaceWorkerScheduleRecurrenceType.MONTHLY, nth, start, end);
    }

    private WorkspaceWorkerSchedule rule(DayOfWeek startDay, String startTime, DayOfWeek endDay, String endTime,
        WorkspaceWorkerScheduleRecurrenceType type, Integer nth, String startDate, String endDate) {
        return WorkspaceWorkerSchedule.create(mock(WorkspaceWorker.class), new FixedWorkerScheduleCommand(
            startDay, LocalTime.parse(startTime), endDay, LocalTime.parse(endTime),
            type, nth, startDate == null ? null : LocalDate.parse(startDate), endDate == null ? null : LocalDate.parse(endDate)
        ));
    }
}
