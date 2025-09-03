package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
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
import com.querydsl.jpa.JPAExpressions;
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
    public ReputationRequest findById(Long requestId) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;

        return queryFactory.selectFrom(qReputationRequest)
            .where(qReputationRequest.id.eq(requestId)
                .and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED))
            )
            .fetchOne();
    }

    @Override
    public ReputationRequest findByTargetAndId(ReputationType targetType, Long targetId, Long requestId) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;

        return queryFactory.selectFrom(qReputationRequest)
            .where(qReputationRequest.targetType.eq(targetType)
                .and(qReputationRequest.targetId.eq(targetId))
                .and(qReputationRequest.id.eq(requestId))
                .and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED))
            )
            .fetchOne();
    }

    private BooleanExpression cursorCondition(QReputationRequest qReputationRequest, CursorDto cursor) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return qReputationRequest.createdAt.lt(cursor.getCreatedAt())
            .or(qReputationRequest.createdAt.eq(cursor.getCreatedAt())
                .and(qReputationRequest.id.lt(cursor.getId())));
    }

    private BooleanExpression buildBaseWhereConditions(
        QReputationRequest qReputationRequest,
        ReputationType targetType,
        Long targetId,
        ReputationRequestFilterDto filter
    ) {
        BooleanExpression condition = null;

        // 상태 필터링 - filter가 null이거나 status가 null이면 상태 조건 없음
        if (filter != null && filter.getStatus() != null) {
            condition = qReputationRequest.status.eq(filter.getStatus());
        }

        if (ReputationType.USER.equals(targetType)) {
            BooleanExpression targetCondition = qReputationRequest.targetType.eq(targetType)
                .and(qReputationRequest.targetId.eq(targetId));

            condition = condition != null ? condition.and(targetCondition) : targetCondition;
        } else if (ReputationType.WORKSPACE.equals(targetType)) {
            BooleanExpression targetCondition;

            if (filter != null && filter.getWorkspaceId() != null) {
                // 특정 업장의 요청만 조회
                targetCondition = qReputationRequest.targetType.eq(ReputationType.WORKSPACE)
                    .and(qReputationRequest.targetId.eq(filter.getWorkspaceId()));
            } else {
                // 매니저가 관리하는 모든 업장의 요청 조회 (targetId는 매니저 userId)
                QWorkspace qWorkspace = QWorkspace.workspace;
                targetCondition = qReputationRequest.targetType.eq(ReputationType.WORKSPACE)
                    .and(qReputationRequest.targetId.in(
                        JPAExpressions.select(qWorkspace.id)
                            .from(qWorkspace)
                            .where(qWorkspace.managerUser.id.eq(targetId))
                    ));
            }

            condition = condition != null ? condition.and(targetCondition) : targetCondition;
        }

        return condition;
    }

    @Override
    public long getCountOfReputationRequestsByUser(Long userId, ReputationRequestStatus status) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;

        BooleanExpression condition = qReputationRequest.targetType.eq(ReputationType.USER)
            .and(qReputationRequest.targetId.eq(userId));

        if (status != null) {
            condition = condition.and(qReputationRequest.status.eq(status));
        }

        Long count = queryFactory
            .select(qReputationRequest.count())
            .from(qReputationRequest)
            .where(condition)
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ReputationRequestListResponse> getReputationRequestsWithCursorByUser(
        CursorPageRequest<CursorDto> pageRequest,
        Long userId,
        ReputationRequestStatus status
    ) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;
        QUser qRequesterUser = new QUser("qRequesterUser");
        QWorkspace qRequesterWorkspace = new QWorkspace("qRequesterWorkspace");
        QUser qTargetUser = new QUser("qTargetUser");

        BooleanExpression condition = qReputationRequest.targetType.eq(ReputationType.USER)
            .and(qReputationRequest.targetId.eq(userId))
            .and(cursorCondition(qReputationRequest, pageRequest.cursor()));

        if (status != null) {
            condition = condition.and(qReputationRequest.status.eq(status));
        }

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
                qTargetUser.name,
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
            .where(condition)
            .orderBy(qReputationRequest.createdAt.desc(), qReputationRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getCountOfReputationRequestsByWorkspace(List<Long> workspaceIds, ReputationRequestFilterDto filter) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;

        BooleanExpression condition = qReputationRequest.targetType.eq(ReputationType.WORKSPACE);

        // workspaceId 필터링
        if (filter != null && filter.getWorkspaceId() != null) {
            condition = condition.and(qReputationRequest.targetId.eq(filter.getWorkspaceId()));
        } else {
            condition = condition.and(qReputationRequest.targetId.in(workspaceIds));
        }

        // 조회 시 기본적으로 REQUESTED 상태인 항목만 조회
        if (filter != null && filter.getStatus() != null) {
            condition = condition.and(qReputationRequest.status.eq(filter.getStatus()));
        } else {
            condition = condition.and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED));
        }

        Long count = queryFactory
            .select(qReputationRequest.count())
            .from(qReputationRequest)
            .where(condition)
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ReputationRequestListResponse> getReputationRequestsWithCursorByWorkspace(
        CursorPageRequest<CursorDto> pageRequest,
        List<Long> workspaceIds,
        ReputationRequestFilterDto filter
    ) {
        QReputationRequest qReputationRequest = QReputationRequest.reputationRequest;
        QUser qRequesterUser = new QUser("qRequesterUser");
        QWorkspace qRequesterWorkspace = new QWorkspace("qRequesterWorkspace");
        QWorkspace qTargetWorkspace = new QWorkspace("qTargetWorkspace");

        BooleanExpression condition = qReputationRequest.targetType.eq(ReputationType.WORKSPACE)
            .and(cursorCondition(qReputationRequest, pageRequest.cursor()));

        // workspaceId 필터링
        if (filter != null && filter.getWorkspaceId() != null) {
            condition = condition.and(qReputationRequest.targetId.eq(filter.getWorkspaceId()));
        } else {
            condition = condition.and(qReputationRequest.targetId.in(workspaceIds));
        }

        // 조회 시 기본적으로 REQUESTED 상태인 항목만 조회
        if (filter != null && filter.getStatus() != null) {
            condition = condition.and(qReputationRequest.status.eq(filter.getStatus()));
        } else {
            condition = condition.and(qReputationRequest.status.eq(ReputationRequestStatus.REQUESTED));
        }

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
                qTargetWorkspace.businessName,
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
            .leftJoin(qTargetWorkspace)
            .on(qReputationRequest.targetType.eq(ReputationType.WORKSPACE)
                .and(qReputationRequest.targetId.eq(qTargetWorkspace.id)))
            .where(condition)
            .orderBy(qReputationRequest.createdAt.desc(), qReputationRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }
}
