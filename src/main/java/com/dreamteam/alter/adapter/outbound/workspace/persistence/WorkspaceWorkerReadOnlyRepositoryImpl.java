package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerReadOnlyRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceWorkerReadOnlyRepositoryImpl implements WorkspaceWorkerReadOnlyRepository {

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

}
