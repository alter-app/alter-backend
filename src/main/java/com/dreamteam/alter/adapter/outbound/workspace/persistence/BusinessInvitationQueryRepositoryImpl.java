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
    public long countByUser(User user, MyInvitationListFilterDto filter) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, filter),
                dateFromCondition(q, filter),
                dateToCondition(q, filter)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<BusinessInvitation> findByUserWithCursor(CursorPageRequest<CursorDto> pageRequest, User user, MyInvitationListFilterDto filter) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.invitedUser.eq(user),
                statusCondition(q, filter),
                dateFromCondition(q, filter),
                dateToCondition(q, filter),
                cursorCondition(q, pageRequest.cursor())
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression statusCondition(QBusinessInvitation q, MyInvitationListFilterDto filter) {
        if (filter == null || filter.getStatus() == null) {
            return null;
        }
        return q.status.eq(filter.getStatus());
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
