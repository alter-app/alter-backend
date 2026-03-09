package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.QBusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BusinessJoinRequestQueryRepositoryImpl implements BusinessJoinRequestQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BusinessJoinRequest> findById(Long id) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return Optional.ofNullable(
            queryFactory.selectFrom(qBusinessJoinRequest)
                .where(qBusinessJoinRequest.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public boolean existsPendingRequest(Workspace workspace, User user) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(qBusinessJoinRequest.count())
            .from(qBusinessJoinRequest)
            .where(qBusinessJoinRequest.workspace.eq(workspace)
                .and(qBusinessJoinRequest.user.eq(user))
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public long countByUser(User user, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.user.eq(user),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public long countByWorkspace(Workspace workspace, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.workspace.eq(workspace),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<BusinessJoinRequest> findByUserWithCursor(User user, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.user.eq(user),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to),
                cursorCondition(q, cursor)
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageSize)
            .fetch();
    }

    @Override
    public List<BusinessJoinRequest> findByWorkspaceWithCursor(Workspace workspace, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(q)
            .join(q.user).fetchJoin()
            .where(
                q.workspace.eq(workspace),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to),
                cursorCondition(q, cursor)
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageSize)
            .fetch();
    }

    private BooleanExpression statusCondition(QBusinessJoinRequest q, BusinessJoinRequestStatus status) {
        if (status == null) {
            return null;
        }
        return q.status.eq(status);
    }

    private BooleanExpression dateFromCondition(QBusinessJoinRequest q, LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return q.createdAt.goe(from);
    }

    private BooleanExpression dateToCondition(QBusinessJoinRequest q, LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return q.createdAt.lt(to);
    }

    private BooleanExpression cursorCondition(QBusinessJoinRequest q, CursorDto cursor) {
        if (cursor == null || cursor.getId() == null) {
            return null;
        }
        if (cursor.getCreatedAt() != null) {
            return q.createdAt.lt(cursor.getCreatedAt())
                .or(q.createdAt.eq(cursor.getCreatedAt()).and(q.id.lt(cursor.getId())));
        }
        return q.id.lt(cursor.getId());
    }
}
