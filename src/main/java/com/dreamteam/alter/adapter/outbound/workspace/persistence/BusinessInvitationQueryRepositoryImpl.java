package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.QBusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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
    public boolean existsPendingInvitation(Workspace workspace, User user) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        Long count = queryFactory
            .select(qBusinessInvitation.count())
            .from(qBusinessInvitation)
            .where(qBusinessInvitation.workspace.eq(workspace)
                .and(qBusinessInvitation.invitedUser.eq(user))
                .and(qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING)))
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public List<BusinessInvitation> findPendingByUser(User user) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(qBusinessInvitation)
            .join(qBusinessInvitation.workspace).fetchJoin()
            .where(qBusinessInvitation.invitedUser.eq(user)
                .and(qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING)))
            .fetch();
    }

    @Override
    public Set<Long> findPendingInvitedUserIds(Long workspaceId) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return new HashSet<>(queryFactory
            .select(qBusinessInvitation.invitedUser.id)
            .from(qBusinessInvitation)
            .where(
                qBusinessInvitation.workspace.id.eq(workspaceId),
                qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING)
            )
            .fetch());
    }

    @Override
    public List<BusinessInvitation> findByUserWithCursor(User user, BusinessInvitationStatus status, CursorDto cursor, int pageSize) {
        QBusinessInvitation q = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.invitedUser.eq(user),
                status != null ? q.status.eq(status) : null,
                cursorCondition(q, cursor)
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageSize)
            .fetch();
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
