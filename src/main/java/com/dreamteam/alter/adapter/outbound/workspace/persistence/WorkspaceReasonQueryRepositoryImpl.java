package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonListResponse;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceReason;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceReasonQueryRepositoryImpl implements WorkspaceReasonQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WorkspaceReason> findByIdAndWorkspaceRequestId(Long reasonId, Long workspaceRequestId) {
        QWorkspaceReason qWorkspaceReason = QWorkspaceReason.workspaceReason;
        return Optional.ofNullable(
            queryFactory
                .selectFrom(qWorkspaceReason)
                .where(
                    qWorkspaceReason.id.eq(reasonId),
                    qWorkspaceReason.workspaceRequest.id.eq(workspaceRequestId)
                )
                .fetchOne()
        );
    }

    @Override
    public List<WorkspaceReasonListResponse> getWorkspaceReasonList(Long workspaceRequestId) {
        QWorkspaceReason qWorkspaceReason = QWorkspaceReason.workspaceReason;
        return queryFactory
            .select(Projections.constructor(
                WorkspaceReasonListResponse.class,
                qWorkspaceReason.id,
                qWorkspaceReason.reason,
                qWorkspaceReason.createdAt
            ))
            .from(qWorkspaceReason)
            .where(qWorkspaceReason.workspaceRequest.id.eq(workspaceRequestId))
            .orderBy(qWorkspaceReason.createdAt.desc())
            .fetch();
    }
}
