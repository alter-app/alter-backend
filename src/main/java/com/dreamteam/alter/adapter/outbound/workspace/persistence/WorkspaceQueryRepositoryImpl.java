package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceQueryRepositoryImpl implements WorkspaceQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Workspace> findById(Long id) {
        QWorkspace qWorkspace = QWorkspace.workspace;

        Workspace workspace = queryFactory
            .select(qWorkspace)
            .from(qWorkspace)
            .where(qWorkspace.id.eq(id))
            .fetchOne();

        return ObjectUtils.isEmpty(workspace) ? Optional.empty() : Optional.of(workspace);
    }

}
