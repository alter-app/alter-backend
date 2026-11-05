package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostingScheduleTests {
    @ParameterizedTest
    @ValueSource(strings = {"00:00", "09:30"})
    void create_rejectsZeroMinuteSchedule(String value) {
        LocalTime time = LocalTime.parse(value);
        assertThatThrownBy(() -> PostingSchedule.create(List.of(DayOfWeek.MONDAY), time, time, "홀", null))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "09:30"})
    void update_rejectsZeroMinuteScheduleBeforeChangingFields(String value) {
        PostingSchedule schedule = PostingSchedule.create(List.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0), LocalTime.of(18, 0), "홀", null);
        LocalTime time = LocalTime.parse(value);

        assertThatThrownBy(() -> schedule.update(List.of(DayOfWeek.FRIDAY), time, time, "주방"))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(schedule.getWorkingDays()).containsExactly(DayOfWeek.MONDAY);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(schedule.getPosition()).isEqualTo("홀");
    }

    @ParameterizedTest
    @ValueSource(strings = {"09:00,18:00", "22:00,06:00"})
    void createAndUpdate_preserveDaytimeAndOvernightSchedules(String value) {
        String[] times = value.split(",");
        LocalTime start = LocalTime.parse(times[0]);
        LocalTime end = LocalTime.parse(times[1]);
        PostingSchedule schedule = PostingSchedule.create(List.of(DayOfWeek.MONDAY), start, end, "홀", null);
        assertThat(schedule.getStartTime()).isEqualTo(start);
        assertThat(schedule.getEndTime()).isEqualTo(end);

        schedule.update(List.of(DayOfWeek.FRIDAY), start, end, "주방");
        assertThat(schedule.getStartTime()).isEqualTo(start);
        assertThat(schedule.getEndTime()).isEqualTo(end);
        assertThat(schedule.getWorkingDays()).containsExactly(DayOfWeek.FRIDAY);
        assertThat(schedule.getPosition()).isEqualTo("주방");
    }
}
