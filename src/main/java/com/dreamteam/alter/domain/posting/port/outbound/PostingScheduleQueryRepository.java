package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;

import java.util.Optional;

public interface PostingScheduleQueryRepository {
    Optional<PostingSchedule> findByIdAndPostingId(Long postingId, Long postingScheduleId);
}
