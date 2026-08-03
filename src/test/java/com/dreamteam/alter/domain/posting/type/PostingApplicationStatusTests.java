package com.dreamteam.alter.domain.posting.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostingApplicationStatus 테스트")
class PostingApplicationStatusTests {

    @Test
    @DisplayName("ACTIVE_STATUSES 는 지원 완료·서류 합격·최종 합격만 포함한다")
    void activeStatusesContainsOnlyLiveApplications() {
        // when & then
        assertThat(PostingApplicationStatus.ACTIVE_STATUSES)
            .containsExactlyInAnyOrder(
                PostingApplicationStatus.SUBMITTED,
                PostingApplicationStatus.SHORTLISTED,
                PostingApplicationStatus.ACCEPTED
            );
    }
}
