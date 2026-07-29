package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.domain.posting.type.PostingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
