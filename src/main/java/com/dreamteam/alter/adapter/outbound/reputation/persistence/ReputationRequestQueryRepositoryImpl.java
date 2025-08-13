package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.entity.QReputationRequest;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
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
                .and(reputationRequest.expiresAt.before(now)))
            .fetch();
    }

    @Override
    public long getCountOfReputationRequestsByTarget(ReputationType targetType, Long targetId) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;

        Long count = queryFactory
            .select(qReputationRequest.count())
            .from(qReputationRequest)
            .where(qReputationRequest.targetType.eq(targetType)
                .and(qReputationRequest.targetId.eq(targetId))
                .and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED))
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ReputationRequestListResponse> getReputationRequestsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        ReputationType targetType,
        Long targetId
    ) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;
        QUser qRequesterUser = new QUser("qRequesterUser");
        QWorkspace qRequesterWorkspace = new QWorkspace("qRequesterWorkspace");
        QUser qTargetUser = new QUser("qTargetUser");
        QWorkspace qTargetWorkspace = new QWorkspace("qTargetWorkspace");

        return queryFactory
            .select(Projections.constructor(
                ReputationRequestListResponse.class,
                qReputationRequest.id,
                qReputationRequest.requestType,
                qReputationRequest.requesterType,
                qReputationRequest.requesterId,
                qRequesterUser.name.coalesce(qRequesterWorkspace.businessName),
                qReputationRequest.targetType,
                qReputationRequest.targetId,
                qTargetUser.name.coalesce(qTargetWorkspace.businessName),
                qReputationRequest.status,
                qReputationRequest.createdAt,
                qReputationRequest.expiresAt
            ))
            .from(qReputationRequest)
            .leftJoin(qRequesterUser)
            .on(qReputationRequest.requesterType.eq(ReputationType.USER)
                .and(qReputationRequest.requesterId.eq(qRequesterUser.id)))
            .leftJoin(qRequesterWorkspace)
            .on(qReputationRequest.requesterType.eq(ReputationType.WORKSPACE)
                .and(qReputationRequest.requesterId.eq(qRequesterWorkspace.id)))
            .leftJoin(qTargetUser)
            .on(qReputationRequest.targetType.eq(ReputationType.USER)
                .and(qReputationRequest.targetId.eq(qTargetUser.id)))
            .leftJoin(qTargetWorkspace)
            .on(qReputationRequest.targetType.eq(ReputationType.WORKSPACE)
                .and(qReputationRequest.targetId.eq(qTargetWorkspace.id)))
            .where(qReputationRequest.targetType.eq(targetType)
                .and(qReputationRequest.targetId.eq(targetId))
                .and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED))
                .and(cursorCondition(qReputationRequest, pageRequest.cursor())))
            .orderBy(qReputationRequest.createdAt.desc(), qReputationRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression cursorCondition(QReputationRequest qReputationRequest, CursorDto cursor) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return qReputationRequest.createdAt.lt(cursor.getCreatedAt())
            .or(qReputationRequest.createdAt.eq(cursor.getCreatedAt())
                .and(qReputationRequest.id.lt(cursor.getId())));
    }

}
