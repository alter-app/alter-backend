package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;

import java.util.Optional;
import java.util.List;

public interface PostingScheduleQueryRepository {
    Optional<PostingSchedule> findByIdAndPostingId(Long postingId, Long postingScheduleId);
    void initializeWorkingDaysByIds(List<Long> scheduleIds);
}
