package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.QReputationRequest;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReputationRequestQueryRepositoryImpl implements ReputationRequestQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReputationRequest> findAllByStatusAndExpiredAtBefore(
        ReputationRequestStatus status,
        LocalDateTime now
    ) {
        QReputationRequest reputationRequest = QReputationRequest.reputationRequest;

        return queryFactory.selectFrom(reputationRequest)
            .where(reputationRequest.status.eq(status)
                .and(reputationRequest.expiredAt.before(now)))
            .fetch();
    }

}
