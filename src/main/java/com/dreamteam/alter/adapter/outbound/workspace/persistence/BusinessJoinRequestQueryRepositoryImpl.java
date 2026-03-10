package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestListFilterDto;
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

import java.time.LocalDate;
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
    public long countByUser(User user, MyJoinRequestListFilterDto filter) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.user.eq(user),
                statusCondition(q, filter != null ? filter.getStatus() : null),
                dateFromCondition(q, filter != null ? filter.getFrom() : null),
                dateToCondition(q, filter != null ? filter.getTo() : null)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public long countByWorkspace(Workspace workspace, WorkspaceJoinRequestListFilterDto filter) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(q.count())
            .from(q)
            .where(
                q.workspace.eq(workspace),
                statusCondition(q, filter != null ? filter.getStatus() : null),
                dateFromCondition(q, filter != null ? filter.getFrom() : null),
                dateToCondition(q, filter != null ? filter.getTo() : null)
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<BusinessJoinRequest> findByUserWithCursor(CursorPageRequest<CursorDto> pageRequest, User user, MyJoinRequestListFilterDto filter) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(q)
            .join(q.workspace).fetchJoin()
            .where(
                q.user.eq(user),
                statusCondition(q, filter != null ? filter.getStatus() : null),
                dateFromCondition(q, filter != null ? filter.getFrom() : null),
                dateToCondition(q, filter != null ? filter.getTo() : null),
                cursorCondition(q, pageRequest.cursor())
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public List<BusinessJoinRequest> findByWorkspaceWithCursor(CursorPageRequest<CursorDto> pageRequest, Workspace workspace, WorkspaceJoinRequestListFilterDto filter) {
        QBusinessJoinRequest q = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(q)
            .join(q.user).fetchJoin()
            .where(
                q.workspace.eq(workspace),
                statusCondition(q, filter != null ? filter.getStatus() : null),
                dateFromCondition(q, filter != null ? filter.getFrom() : null),
                dateToCondition(q, filter != null ? filter.getTo() : null),
                cursorCondition(q, pageRequest.cursor())
            )
            .orderBy(q.createdAt.desc(), q.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression statusCondition(QBusinessJoinRequest q, BusinessJoinRequestStatus status) {
        return status != null ? q.status.eq(status) : null;
    }

    private BooleanExpression dateFromCondition(QBusinessJoinRequest q, LocalDate from) {
        return from != null ? q.createdAt.goe(from.atStartOfDay()) : null;
    }

    private BooleanExpression dateToCondition(QBusinessJoinRequest q, LocalDate to) {
        return to != null ? q.createdAt.lt(to.plusDays(1).atStartOfDay()) : null;
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
