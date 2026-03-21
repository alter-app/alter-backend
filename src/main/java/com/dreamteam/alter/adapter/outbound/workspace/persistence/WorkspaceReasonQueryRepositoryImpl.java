package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.QWorkspaceReason;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceReason;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceReasonQueryRepositoryImpl implements WorkspaceReasonQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WorkspaceReason> findByIdAndWorkspaceId(Long reasonId, Long workspaceId) {
        QWorkspaceReason qWorkspaceReason = QWorkspaceReason.workspaceReason;
        return Optional.ofNullable(
            queryFactory
                .selectFrom(qWorkspaceReason)
                .where(
                    qWorkspaceReason.id.eq(reasonId),
                    qWorkspaceReason.workspace.id.eq(workspaceId)
                )
                .fetchOne()
        );
    }
}
