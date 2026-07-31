package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Posting 테스트")
class PostingTests {

    @Test
    @DisplayName("삭제된 근무일정은 활성 일정 목록에서 제외된다")
    void getActiveSchedules_삭제일정제외() {
        // given
        Posting posting = new Posting();
        PostingSchedule open = createSchedule(posting, "홀서빙");
        PostingSchedule deleted = createSchedule(posting, "주방보조");
        deleted.updateStatus(PostingStatus.DELETED);
        ReflectionTestUtils.setField(posting, "schedules", List.of(open, deleted));

        // when
        List<PostingSchedule> result = posting.getActiveSchedules();

        // then
        assertThat(result).containsExactly(open);
    }

    @Test
    @DisplayName("근무일정이 없으면 빈 목록을 반환한다")
    void getActiveSchedules_일정없음_빈목록() {
        // given
        Posting posting = new Posting();

        // when & then
        assertThat(posting.getActiveSchedules()).isEmpty();
    }

    @Test
    @DisplayName("삭제된 근무일정은 수정할 수 없다")
    void updateSchedules_삭제일정_예외발생() {
        // given
        Posting posting = new Posting();
        PostingSchedule deleted = createSchedule(posting, 1L, "주방보조");
        deleted.updateStatus(PostingStatus.DELETED);
        ReflectionTestUtils.setField(posting, "schedules", List.of(deleted));

        UpdatePostingScheduleCommand command = new UpdatePostingScheduleCommand(
            1L,
            List.of(DayOfWeek.TUESDAY),
            LocalTime.of(10, 0),
            LocalTime.of(19, 0),
            5,
            "홀서빙"
        );

        // when & then
        assertThatThrownBy(() -> posting.updateSchedules(List.of(command)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        assertThat(deleted.getPosition()).isEqualTo("주방보조");
        assertThat(deleted.getPositionsAvailable()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 삭제된 근무일정은 다시 삭제할 수 없다")
    void deleteSchedules_삭제일정_예외발생() {
        // given
        Posting posting = new Posting();
        PostingSchedule deleted = createSchedule(posting, 1L, "주방보조");
        deleted.updateStatus(PostingStatus.DELETED);
        ReflectionTestUtils.setField(posting, "schedules", List.of(deleted));

        // when & then
        assertThatThrownBy(() -> posting.deleteSchedules(List.of(1L)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    private PostingSchedule createSchedule(Posting posting, String position) {
        return PostingSchedule.create(
            List.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
            1,
            position,
            posting
        );
    }

    private PostingSchedule createSchedule(Posting posting, Long id, String position) {
        PostingSchedule schedule = createSchedule(posting, position);
        ReflectionTestUtils.setField(schedule, "id", id);
        return schedule;
    }
}
