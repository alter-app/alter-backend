package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationListFilterDto;
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
    public Set<Long> findPendingInvitedUserIdsByUserIds(Long workspaceId, Set<Long> userIds, LocalDateTime now) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return new HashSet<>(queryFactory
            .select(qBusinessInvitation.invitedUser.id)
            .from(qBusinessInvitation)
            .where(
                qBusinessInvitation.workspace.id.eq(workspaceId),
                qBusinessInvitation.invitedUser.id.in(userIds),
                pendingAndValid(qBusinessInvitation, now)
            )
            .fetch());
    }

    @Override
    public List<BusinessInvitation> findExpiredPendingByWorkspaceAndUserIds(Long workspaceId, Set<Long> userIds, LocalDateTime now) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(qBusinessInvitation)
            .where(
                qBusinessInvitation.workspace.id.eq(workspaceId),
                qBusinessInvitation.invitedUser.id.in(userIds),
                pendingAndExpired(qBusinessInvitation, now)
            )
            .fetch();
    }

    @Override
    public long countByUser(User user, MyInvitationListFilterDto filter, LocalDateTime now) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, filter, now),
                dateFromCondition(q, filter),
                dateToCondition(q, filter)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<BusinessInvitation> findByUserWithCursor(CursorPageRequest<CursorDto> pageRequest, User user, MyInvitationListFilterDto filter, LocalDateTime now) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, filter, now),
                dateFromCondition(q, filter),
                dateToCondition(q, filter),
                cursorCondition(q, pageRequest.cursor())
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression pendingAndValid(QBusinessInvitation q, LocalDateTime now) {
        return q.status.eq(BusinessInvitationStatus.PENDING).and(q.expiresAt.gt(now));
    }

    private BooleanExpression pendingAndExpired(QBusinessInvitation q, LocalDateTime now) {
        return q.status.eq(BusinessInvitationStatus.PENDING).and(q.expiresAt.loe(now));
    }

    private BooleanExpression statusCondition(QBusinessInvitation q, MyInvitationListFilterDto filter, LocalDateTime now) {
        BusinessInvitationStatus status = filter == null ? null : filter.getStatus();
        if (status == null) {
            return q.status.ne(BusinessInvitationStatus.EXPIRED).and(pendingAndExpired(q, now).not());
        }
        if (BusinessInvitationStatus.PENDING.equals(status)) {
            return pendingAndValid(q, now);
        }
        if (BusinessInvitationStatus.EXPIRED.equals(status)) {
            return q.status.eq(BusinessInvitationStatus.EXPIRED).or(pendingAndExpired(q, now));
        }
        return q.status.eq(status);
    }

    private BooleanExpression dateFromCondition(QBusinessInvitation q, MyInvitationListFilterDto filter) {
        if (filter == null || filter.getFrom() == null) {
            return null;
        }
        return q.createdAt.goe(filter.getFrom().atStartOfDay());
    }

    private BooleanExpression dateToCondition(QBusinessInvitation q, MyInvitationListFilterDto filter) {
        if (filter == null || filter.getTo() == null) {
            return null;
        }
        return q.createdAt.lt(filter.getTo().plusDays(1).atStartOfDay());
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
