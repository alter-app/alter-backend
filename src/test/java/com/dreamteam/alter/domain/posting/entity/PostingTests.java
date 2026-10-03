package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
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
            "홀서빙"
        );

        // when & then
        assertThatThrownBy(() -> posting.updateSchedules(List.of(command)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        assertThat(deleted.getPosition()).isEqualTo("주방보조");
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

    @Test
    @DisplayName("마지막 근무일정 삭제는 거부하고 공고 상태를 유지한다")
    void updateContent_마지막일정삭제_예외발생() {
        // given
        Posting posting = openPosting();
        PostingSchedule only = createSchedule(posting, 1L, "홀서빙");
        ReflectionTestUtils.setField(posting, "schedules", new ArrayList<>(List.of(only)));

        assertThatThrownBy(() -> posting.updateContent(updateCommand(null, null, List.of(1L))))
            .isInstanceOf(CustomException.class)
            .hasMessage("근무 일정은 최소 1개 이상 있어야 합니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.OPEN);
    }

    @Test
    void workingDays_중복없이_요일순서로반환하고_수정한다() {
        PostingSchedule schedule = PostingSchedule.create(
            List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0), LocalTime.of(18, 0), "홀서빙", openPosting()
        );
        assertThat(schedule.getWorkingDays()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        Object daysCollection = ReflectionTestUtils.getField(schedule, "workingDays");
        schedule.update(List.of(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY),
            LocalTime.of(10, 0), LocalTime.of(19, 0), "주방보조");
        assertThat(schedule.getWorkingDays()).containsExactly(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY);
        assertThat(ReflectionTestUtils.getField(schedule, "workingDays")).isSameAs(daysCollection);
    }

    @Test
    void recruitCount_생성과수정에반영하고_공고상태를유지한다() {
        PostingScheduleCommand schedule = new PostingScheduleCommand(
            List.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(18, 0), "홀서빙");
        Posting posting = Posting.create(new CreatePostingCommand(
            1L, "제목", "설명", 12000, 2, PaymentType.HOURLY, List.of(schedule)), null);
        assertThat(posting.getRecruitCount()).isEqualTo(2);
        posting.updateStatus(PostingStatus.CLOSED);
        posting.updateContent(updateCommand(null, null, null));
        assertThat(posting.getRecruitCount()).isEqualTo(3);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.CLOSED);
    }

    @Test
    void updateContent_마감된공고도_활성일정이없으면거부한다() {
        Posting posting = openPosting();
        posting.updateStatus(PostingStatus.CLOSED);
        ReflectionTestUtils.setField(posting, "schedules", new ArrayList<>());
        assertThatThrownBy(() -> posting.updateContent(updateCommand(null, null, null)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.CLOSED);
    }

    @Test
    @DisplayName("근무일정을 전부 삭제하고 새로 추가하면 모집 중을 유지한다")
    void updateContent_전부삭제후추가_모집중유지() {
        // given
        Posting posting = openPosting();
        PostingSchedule only = createSchedule(posting, 1L, "홀서빙");
        ReflectionTestUtils.setField(posting, "schedules", new ArrayList<>(List.of(only)));

        PostingScheduleCommand created = new PostingScheduleCommand(
            List.of(DayOfWeek.FRIDAY),
            LocalTime.of(13, 0),
            LocalTime.of(21, 0),
            "주방보조"
        );

        // when
        posting.updateContent(updateCommand(List.of(created), null, List.of(1L)));

        // then
        assertThat(posting.getActiveSchedules()).hasSize(1);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.OPEN);
    }

    @Test
    @DisplayName("근무일정이 남아 있으면 모집 중을 유지한다")
    void updateContent_일부일정삭제_모집중유지() {
        // given
        Posting posting = openPosting();
        PostingSchedule first = createSchedule(posting, 1L, "홀서빙");
        PostingSchedule second = createSchedule(posting, 2L, "주방보조");
        ReflectionTestUtils.setField(posting, "schedules", new ArrayList<>(List.of(first, second)));

        // when
        posting.updateContent(updateCommand(null, null, List.of(1L)));

        // then
        assertThat(posting.getActiveSchedules()).containsExactly(second);
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.OPEN);
    }

    @Test
    @DisplayName("삭제된 공고는 내용을 수정할 수 없다")
    void updateContent_삭제공고_예외발생() {
        // given
        Posting posting = new Posting();
        ReflectionTestUtils.setField(posting, "status", PostingStatus.DELETED);
        ReflectionTestUtils.setField(posting, "title", "원래 제목");

        // when & then
        assertThatThrownBy(() -> posting.updateContent(updateCommand(null, null, null)))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        assertThat(posting.getTitle()).isEqualTo("원래 제목");
    }

    @Test
    @DisplayName("삭제된 공고는 상태를 변경할 수 없다")
    void updateStatus_삭제공고_예외발생() {
        // given
        Posting posting = new Posting();
        ReflectionTestUtils.setField(posting, "status", PostingStatus.DELETED);

        // when & then
        assertThatThrownBy(() -> posting.updateStatus(PostingStatus.OPEN))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        assertThat(posting.getStatus()).isEqualTo(PostingStatus.DELETED);
    }

    private Posting openPosting() {
        Posting posting = new Posting();
        ReflectionTestUtils.setField(posting, "status", PostingStatus.OPEN);
        return posting;
    }

    private UpdatePostingCommand updateCommand(
        List<PostingScheduleCommand> createSchedules,
        List<UpdatePostingScheduleCommand> updateSchedules,
        List<Long> deleteScheduleIds
    ) {
        return new UpdatePostingCommand(
            "제목",
            "설명",
            12000,
            3,
            PaymentType.HOURLY,
            createSchedules,
            updateSchedules,
            deleteScheduleIds
        );
    }

    private PostingSchedule createSchedule(Posting posting, String position) {
        return PostingSchedule.create(
            List.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
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
