package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceWorkerUserResponse;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.core.types.Projections;
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

    @Override
    public List<ManagerWorkspaceListResponse> getManagerWorkspaceList(ManagerUser managerUser) {
        QWorkspace qWorkspace = QWorkspace.workspace;

        return queryFactory
            .select(
                Projections.constructor(
                    ManagerWorkspaceListResponse.class,
                    qWorkspace.id,
                    qWorkspace.businessName,
                    qWorkspace.fullAddress,
                    qWorkspace.createdAt,
                    qWorkspace.status
                )
            )
            .from(qWorkspace)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.status.ne(WorkspaceStatus.DELETED)
            )
            .orderBy(qWorkspace.createdAt.desc(), qWorkspace.id.desc())
            .fetch();
    }

    @Override
    public Optional<ManagerWorkspaceResponse> getByManagerUserAndId(ManagerUser managerUser, Long workspaceId) {
        QWorkspace qWorkspace = QWorkspace.workspace;

        return Optional.ofNullable(
            queryFactory
                .select(
                    Projections.constructor(
                        ManagerWorkspaceResponse.class,
                        qWorkspace.id,
                        qWorkspace.businessRegistrationNo,
                        qWorkspace.businessName,
                        qWorkspace.businessType,
                        qWorkspace.contact,
                        qWorkspace.description,
                        qWorkspace.status,
                        qWorkspace.fullAddress,
                        qWorkspace.latitude,
                        qWorkspace.longitude,
                        qWorkspace.createdAt
                    )
                )
                .from(qWorkspace)
                .where(
                    qWorkspace.managerUser.eq(managerUser),
                    qWorkspace.id.eq(workspaceId),
                    qWorkspace.status.ne(WorkspaceStatus.DELETED)
                )
                .fetchOne()
        );
    }

    @Override
    public long getWorkspaceWorkerCount(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;

        Long count = queryFactory
            .select(qWorkspaceWorker.count())
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .join(qWorkspaceWorker.user, qUser)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId),
                eqWorkerStatus(qWorkspaceWorker, filter.getStatus()),
                likeWorkerName(qUser, filter.getName()),
                gteEmployedAt(qWorkspaceWorker, filter.getEmployedAtFrom()),
                lteEmployedAt(qWorkspaceWorker, filter.getEmployedAtTo()),
                gteResignedAt(qWorkspaceWorker, filter.getResignedAtFrom()),
                lteResignedAt(qWorkspaceWorker, filter.getResignedAtTo())
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ManagerWorkspaceWorkerListResponse> getWorkspaceWorkerList(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        PageRequestDto pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;

        return queryFactory
            .select(Projections.constructor(
                ManagerWorkspaceWorkerListResponse.class,
                qWorkspaceWorker.id,
                Projections.constructor(
                    WorkspaceWorkerUserResponse.class,
                    qUser.id,
                    qUser.name,
                    qUser.contact,
                    qUser.gender
                    ),
                qWorkspaceWorker.status,
                qWorkspaceWorker.employedAt,
                qWorkspaceWorker.resignedAt
            ))
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .join(qWorkspaceWorker.user, qUser)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId),
                eqWorkerStatus(qWorkspaceWorker, filter.getStatus()),
                likeWorkerName(qUser, filter.getName()),
                gteEmployedAt(qWorkspaceWorker, filter.getEmployedAtFrom()),
                lteEmployedAt(qWorkspaceWorker, filter.getEmployedAtTo()),
                gteResignedAt(qWorkspaceWorker, filter.getResignedAtFrom()),
                lteResignedAt(qWorkspaceWorker, filter.getResignedAtTo())
            )
            .orderBy(qWorkspaceWorker.employedAt.desc(), qWorkspaceWorker.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getLimit())
            .fetch();
    }

    private BooleanExpression eqWorkerStatus(QWorkspaceWorker qWorkspaceWorker, WorkspaceWorkerStatus status) {
        return status != null ? qWorkspaceWorker.status.eq(status) : null;
    }

    private BooleanExpression likeWorkerName(QUser qUser, String name) {
        return name != null && !name.trim().isEmpty() ? qUser.name.containsIgnoreCase(name.trim()) : null;
    }

    private BooleanExpression gteEmployedAt(QWorkspaceWorker qWorkspaceWorker, LocalDate employedAtFrom) {
        return employedAtFrom != null ? qWorkspaceWorker.employedAt.goe(employedAtFrom) : null;
    }

    private BooleanExpression lteEmployedAt(QWorkspaceWorker qWorkspaceWorker, LocalDate employedAtTo) {
        return employedAtTo != null ? qWorkspaceWorker.employedAt.loe(employedAtTo) : null;
    }

    private BooleanExpression gteResignedAt(QWorkspaceWorker qWorkspaceWorker, LocalDate resignedAtFrom) {
        return resignedAtFrom != null ? qWorkspaceWorker.resignedAt.goe(resignedAtFrom) : null;
    }

    private BooleanExpression lteResignedAt(QWorkspaceWorker qWorkspaceWorker, LocalDate resignedAtTo) {
        return resignedAtTo != null ? qWorkspaceWorker.resignedAt.loe(resignedAtTo) : null;
    }

}
