package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.entity.QPostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingScheduleQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostingScheduleQueryRepositoryImpl implements PostingScheduleQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<PostingSchedule> findByIdAndPostingId(Long postingId, Long postingScheduleId) {
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;

        PostingSchedule postingSchedule = queryFactory.select(qPostingSchedule)
            .from(qPostingSchedule)
            .where(
                qPostingSchedule.posting.id.eq(postingId),
                qPostingSchedule.id.eq(postingScheduleId)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(postingSchedule) ? Optional.empty() : Optional.of(postingSchedule);
    }

}
