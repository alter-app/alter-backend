package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.QBusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class BusinessInvitationQueryRepositoryImpl implements BusinessInvitationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BusinessInvitation> findById(Long id) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return Optional.ofNullable(
            queryFactory.selectFrom(qBusinessInvitation)
                .where(qBusinessInvitation.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public Set<Long> findPendingInvitedUserIdsByUserIds(Long workspaceId, Set<Long> userIds) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return new HashSet<>(queryFactory
            .select(qBusinessInvitation.invitedUser.id)
            .from(qBusinessInvitation)
            .where(
                qBusinessInvitation.workspace.id.eq(workspaceId),
                qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING),
                qBusinessInvitation.invitedUser.id.in(userIds)
            )
            .fetch());
    }

    @Override
    public long countByUser(User user, BusinessInvitationStatus status, LocalDateTime from, LocalDateTime to) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<BusinessInvitation> findByUserWithCursor(User user, BusinessInvitationStatus status, LocalDateTime from, LocalDateTime to, CursorDto cursor, int pageSize) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, status),
                dateFromCondition(q, from),
                dateToCondition(q, to),
                cursorCondition(q, cursor)
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageSize)
            .fetch();
    }

    private BooleanExpression statusCondition(QBusinessInvitation q, BusinessInvitationStatus status) {
        if (status == null) {
            return null;
        }
        return q.status.eq(status);
    }

    private BooleanExpression dateFromCondition(QBusinessInvitation q, LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return q.createdAt.goe(from);
    }

    private BooleanExpression dateToCondition(QBusinessInvitation q, LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return q.createdAt.lt(to);
    }

    private BooleanExpression cursorCondition(QBusinessInvitation q, CursorDto cursor) {
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
