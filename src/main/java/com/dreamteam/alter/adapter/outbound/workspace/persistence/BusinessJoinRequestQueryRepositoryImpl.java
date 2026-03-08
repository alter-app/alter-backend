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
    public List<BusinessJoinRequest> findPendingByWorkspace(Workspace workspace) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(qBusinessJoinRequest)
            .join(qBusinessJoinRequest.user).fetchJoin()
            .where(qBusinessJoinRequest.workspace.eq(workspace)
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetch();
    }

    @Override
    public List<BusinessJoinRequest> findPendingByUser(User user) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(qBusinessJoinRequest)
            .join(qBusinessJoinRequest.workspace).fetchJoin()
            .where(qBusinessJoinRequest.user.eq(user)
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetch();
    }

    @Override
    public List<BusinessJoinRequest> findByUserWithCursor(User user, BusinessJoinRequestStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.user.eq(user),
                status != null ? q.status.eq(status) : null,
                from != null ? q.createdAt.goe(from) : null,
                to != null ? q.createdAt.lt(to) : null,
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
                status != null ? q.status.eq(status) : null,
                from != null ? q.createdAt.goe(from) : null,
                to != null ? q.createdAt.lt(to) : null,
                cursorCondition(q, cursor)
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageSize)
            .fetch();
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
