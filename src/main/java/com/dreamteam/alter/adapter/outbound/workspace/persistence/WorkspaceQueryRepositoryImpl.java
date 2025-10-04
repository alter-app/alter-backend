package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceManagerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceManagerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceWorkerResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerResponse;
import com.dreamteam.alter.domain.reputation.entity.QReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.QManagerUser;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkerPositionType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceQueryRepositoryImpl implements WorkspaceQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Workspace> findById(Long id) {
        QWorkspace qWorkspace = QWorkspace.workspace;
        return Optional.ofNullable(
            queryFactory
                .selectFrom(qWorkspace)
                .where(qWorkspace.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public List<ManagerWorkspaceListResponse> getManagerWorkspaceList(ManagerUser managerUser) {
        QWorkspace qWorkspace = QWorkspace.workspace;

        return queryFactory
            .select(Projections.constructor(
                ManagerWorkspaceListResponse.class,
                qWorkspace.id,
                qWorkspace.businessName,
                qWorkspace.fullAddress,
                qWorkspace.createdAt,
                qWorkspace.status
            ))
            .from(qWorkspace)
            .where(qWorkspace.managerUser.eq(managerUser))
            .orderBy(qWorkspace.createdAt.desc())
            .fetch();
    }

    @Override
    public ManagerWorkspaceResponse getByManagerUserAndId(ManagerUser managerUser, Long workspaceId) {
        QWorkspace qWorkspace = QWorkspace.workspace;
        QReputationSummary qReputationSummary = QReputationSummary.reputationSummary;

        return queryFactory
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
                    qWorkspace.createdAt,
                    qReputationSummary
                )
            )
            .from(qWorkspace)
            .leftJoin(qReputationSummary)
            .on(
                qReputationSummary.targetType.eq(ReputationType.WORKSPACE),
                qReputationSummary.targetId.eq(qWorkspace.id)
            )
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId)
            )
            .fetchOne();
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
            .select(qWorkspaceWorker.id.count())
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
    public List<ManagerWorkspaceWorkerListResponse> getWorkspaceWorkerListWithCursor(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;
        QWorkspaceShift qWorkspaceShift = QWorkspaceShift.workspaceShift;

        return queryFactory
            .select(Projections.constructor(
                ManagerWorkspaceWorkerListResponse.class,
                qWorkspaceWorker.id,
                Projections.constructor(
                    WorkspaceWorkerResponse.class,
                    qUser.id,
                    qUser.name,
                    qUser.contact,
                    qUser.gender
                ),
                qWorkspaceWorker.status,
                Expressions.constant(WorkerPositionType.WORKER),
                qWorkspaceWorker.employedAt,
                qWorkspaceWorker.resignedAt,
                qWorkspaceWorker.createdAt,
                qWorkspaceShift.startDateTime.min()
            ))
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .join(qWorkspaceWorker.user, qUser)
            .leftJoin(qWorkspaceShift)
            .on(
                qWorkspaceShift.assignedWorkspaceWorker.eq(qWorkspaceWorker),
                qWorkspaceShift.startDateTime.gt(LocalDateTime.now()),
                qWorkspaceShift.status.eq(WorkspaceShiftStatus.CONFIRMED)
            )
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId),
                eqWorkerStatus(qWorkspaceWorker, filter.getStatus()),
                likeWorkerName(qUser, filter.getName()),
                gteEmployedAt(qWorkspaceWorker, filter.getEmployedAtFrom()),
                lteEmployedAt(qWorkspaceWorker, filter.getEmployedAtTo()),
                gteResignedAt(qWorkspaceWorker, filter.getResignedAtFrom()),
                lteResignedAt(qWorkspaceWorker, filter.getResignedAtTo()),
                cursorConditions(qWorkspaceWorker, pageRequest.cursor())
            )
            .groupBy(
                qWorkspaceWorker.id,
                qUser.id,
                qUser.name,
                qUser.contact,
                qUser.gender,
                qWorkspaceWorker.status,
                qWorkspaceWorker.employedAt,
                qWorkspaceWorker.resignedAt,
                qWorkspaceWorker.createdAt
            )
            .orderBy(qUser.name.asc(), qWorkspaceWorker.id.asc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public boolean isUserActiveWorkerInWorkspace(User user, Long workspaceId) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qWorkspaceWorker.id.count())
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .where(
                qWorkspaceWorker.user.eq(user),
                qWorkspace.id.eq(workspaceId),
                qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public long getUserWorkspaceWorkerCount(Long workspaceId) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qWorkspaceWorker.id.count())
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId),
                qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserWorkspaceWorkerListResponse> getUserWorkspaceWorkerListWithCursor(
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;
        QWorkspaceShift qWorkspaceShift = QWorkspaceShift.workspaceShift;

        return queryFactory
            .select(Projections.constructor(
                UserWorkspaceWorkerListResponse.class,
                qWorkspaceWorker.id,
                Projections.constructor(
                    UserWorkspaceWorkerResponse.class,
                    qUser.id,
                    qUser.name
                ),
                Expressions.constant(WorkerPositionType.WORKER),
                qWorkspaceWorker.employedAt,
                qWorkspaceShift.startDateTime.min(),
                qWorkspaceWorker.createdAt
            ))
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .join(qWorkspaceWorker.user, qUser)
            .leftJoin(qWorkspaceShift)
            .on(
                qWorkspaceShift.assignedWorkspaceWorker.eq(qWorkspaceWorker),
                qWorkspaceShift.startDateTime.gt(LocalDateTime.now()),
                qWorkspaceShift.status.eq(WorkspaceShiftStatus.CONFIRMED)
            )
            .where(
                qWorkspace.id.eq(workspaceId),
                qWorkspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED),
                cursorConditions(qWorkspaceWorker, pageRequest.cursor())
            )
            .groupBy(
                qWorkspaceWorker.id,
                qUser.id,
                qUser.name,
                qWorkspaceWorker.employedAt,
                qWorkspaceWorker.createdAt
            )
            .orderBy(qUser.name.asc(), qWorkspaceWorker.id.asc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getUserWorkspaceManagerCount(Long workspaceId) {
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qManagerUser.id.count())
            .from(qManagerUser)
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserWorkspaceManagerListResponse> getUserWorkspaceManagerListWithCursor(
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;

        return queryFactory
            .select(Projections.constructor(
                UserWorkspaceManagerListResponse.class,
                qManagerUser.id,
                Projections.constructor(
                    UserWorkspaceWorkerResponse.class,
                    qUser.id,
                    qUser.name
                ),
                Expressions.constant(WorkerPositionType.OWNER),
                qManagerUser.createdAt
            ))
            .from(qManagerUser)
            .join(qManagerUser.user, qUser)
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId),
                cursorConditions(qManagerUser, pageRequest.cursor())
            )
            .orderBy(qUser.name.asc(), qManagerUser.id.asc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getManagerWorkspaceManagerCount(ManagerUser managerUser, Long workspaceId) {
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qManagerUser.id.count())
            .from(qManagerUser)
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ManagerWorkspaceManagerListResponse> getManagerWorkspaceManagerListWithCursor(
        ManagerUser managerUser,
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;

        return queryFactory
            .select(Projections.constructor(
                ManagerWorkspaceManagerListResponse.class,
                qManagerUser.id,
                Projections.constructor(
                    WorkspaceWorkerResponse.class,
                    qUser.id,
                    qUser.name,
                    qUser.contact,
                    qUser.gender
                ),
                Expressions.constant(WorkerPositionType.OWNER),
                qManagerUser.createdAt
            ))
            .from(qManagerUser)
            .join(qManagerUser.user, qUser)
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                qWorkspace.id.eq(workspaceId),
                cursorConditions(qManagerUser, pageRequest.cursor())
            )
            .orderBy(qUser.name.asc(), qManagerUser.id.asc())
            .limit(pageRequest.pageSize())
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

    private BooleanExpression cursorConditions(QWorkspaceWorker qWorkspaceWorker, CursorDto cursor) {
        if (cursor == null) {
            return null;
        }
        return qWorkspaceWorker.createdAt.lt(cursor.getCreatedAt())
            .or(qWorkspaceWorker.createdAt.eq(cursor.getCreatedAt())
                .and(qWorkspaceWorker.id.lt(cursor.getId())));
    }

    private BooleanExpression cursorConditions(QManagerUser qManagerUser, CursorDto cursor) {
        if (cursor == null) {
            return null;
        }
        return qManagerUser.createdAt.lt(cursor.getCreatedAt())
            .or(qManagerUser.createdAt.eq(cursor.getCreatedAt())
                .and(qManagerUser.id.lt(cursor.getId())));
    }

}