package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceListResponse;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerQueryRepositoryImpl implements WorkspaceWorkerQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WorkspaceWorker> findActiveWorkerByWorkspaceAndUser(Workspace workspace, User user) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;

        return Optional.ofNullable(
            queryFactory.selectFrom(qWorkspaceWorker)
                .where(qWorkspaceWorker.workspace.eq(workspace)
                    .and(qWorkspaceWorker.user.eq(user))
                    .and(qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)))
                .fetchOne()
        );
    }

    @Override
    public Optional<WorkspaceWorker> findById(Long id) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        
        return Optional.ofNullable(
            queryFactory.selectFrom(qWorkspaceWorker)
                .where(qWorkspaceWorker.id.eq(id)
                    .and(qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)))
                .fetchOne()
        );
    }

    @Override
    public long getUserActiveWorkspaceCount(User user) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;

        Long count = queryFactory
            .select(qWorkspaceWorker.count())
            .from(qWorkspaceWorker)
            .where(qWorkspaceWorker.user.eq(user)
                .and(qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)))
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserWorkspaceListResponse> getUserActiveWorkspaceListWithCursor(
        CursorPageRequest<CursorDto> request, 
        User user
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;

        return queryFactory
            .select(Projections.constructor(
                UserWorkspaceListResponse.class,
                qWorkspaceWorker.id,
                qWorkspace.id,
                qWorkspace.businessName,
                qWorkspaceWorker.employedAt,
                qWorkspaceWorker.createdAt
            ))
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .where(
                qWorkspaceWorker.user.eq(user),
                qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED),
                cursorConditions(qWorkspaceWorker, request.cursor())
            )
            .orderBy(qWorkspaceWorker.createdAt.desc())
            .limit(request.pageSize())
            .fetch();
    }


    private BooleanExpression cursorConditions(
        QWorkspaceWorker qWorkspaceWorker, 
        CursorDto cursor
    ) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }
        
        BooleanExpression createdAtCondition = qWorkspaceWorker.createdAt.lt(cursor.getCreatedAt());
        BooleanExpression idCondition = qWorkspaceWorker.id.lt(cursor.getId());
        
        return createdAtCondition.or(
            qWorkspaceWorker.createdAt.eq(cursor.getCreatedAt()).and(idCondition)
        );
    }

}
