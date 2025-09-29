package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.ManagerWorkspaceWorkerListFilterDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.QManagerUser;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
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
                    qWorkspace.id.eq(workspaceId)
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
    public List<ManagerWorkspaceWorkerListResponse> getWorkspaceWorkerList(
        ManagerUser managerUser,
        Long workspaceId,
        ManagerWorkspaceWorkerListFilterDto filter,
        PageRequestDto pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;
        QManagerUser qManagerUser = QManagerUser.managerUser;

        // WorkspaceWorker 조회 (알바생) - 엔티티로 조회
        List<WorkspaceWorker> workspaceWorkers = queryFactory
            .selectFrom(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace).fetchJoin()
            .join(qWorkspaceWorker.user, qUser).fetchJoin()
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
            .orderBy(qUser.name.asc(), qWorkspaceWorker.id.asc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getLimit())
            .fetch();

        // ManagerUser 조회 (점주) - 엔티티로 조회
        List<ManagerUser> managerUsers = queryFactory
            .selectFrom(qManagerUser)
            .join(qManagerUser.user, qUser).fetchJoin()
            .where(
                qManagerUser.eq(managerUser),
                likeWorkerName(qUser, filter.getName())
            )
            .orderBy(qUser.name.asc(), qManagerUser.id.asc())
            .fetch();

        // 점주와 일반 근무자를 통합하여 반환
        List<ManagerWorkspaceWorkerListResponse> allWorkers = new ArrayList<>();
        
        // 1. 점주(매니저) 먼저 추가
        managerUsers.stream()
            .map(ManagerWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        // 2. 일반 근무자(알바생) 나중에 추가
        workspaceWorkers.stream()
            .map(ManagerWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        return allWorkers;
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
        QManagerUser qManagerUser = QManagerUser.managerUser;

        // 점주와 일반 근무자를 통합하여 반환
        List<ManagerWorkspaceWorkerListResponse> allWorkers = new ArrayList<>();
        
        // 1. ManagerUser 조회 (점주) - 우선 조회
        List<ManagerUser> managerUsers = queryFactory
            .selectFrom(qManagerUser)
            .join(qManagerUser.user, qUser).fetchJoin()
            .where(
                qManagerUser.eq(managerUser),
                likeWorkerName(qUser, filter.getName())
            )
            .orderBy(qUser.name.asc(), qManagerUser.id.asc())
            .fetch();

        // 2. WorkspaceWorker 조회 (알바생) - 커서 조건 적용
        List<WorkspaceWorker> workspaceWorkers = queryFactory
            .selectFrom(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace).fetchJoin()
            .join(qWorkspaceWorker.user, qUser).fetchJoin()
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
            .orderBy(qUser.name.asc(), qWorkspaceWorker.id.asc())
            .limit(pageRequest.pageSize())
            .fetch();

        // 3. 점주 먼저 추가, 알바생 나중에 추가
        managerUsers.stream()
            .map(ManagerWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        workspaceWorkers.stream()
            .map(ManagerWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        return allWorkers;
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
    public long getUserWorkspaceWorkerCount(User user, Long workspaceId) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qWorkspaceWorker.id.count())
            .from(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserWorkspaceWorkerListResponse> getUserWorkspaceWorkerList(
        User user,
        Long workspaceId,
        PageRequestDto pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;
        QManagerUser qManagerUser = QManagerUser.managerUser;

        // WorkspaceWorker 조회 (알바생) - 엔티티로 조회
        List<WorkspaceWorker> workspaceWorkers = queryFactory
            .selectFrom(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace).fetchJoin()
            .join(qWorkspaceWorker.user, qUser).fetchJoin()
            .where(
                qWorkspace.id.eq(workspaceId)
            )
            .orderBy(qWorkspaceWorker.createdAt.asc(), qWorkspaceWorker.id.asc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getLimit())
            .fetch();

        // ManagerUser 조회 (점주) - 엔티티로 조회
        List<ManagerUser> managerUsers = queryFactory
            .selectFrom(qManagerUser)
            .join(qManagerUser.user, qUser).fetchJoin()
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId)
            )
            .orderBy(qManagerUser.createdAt.asc(), qManagerUser.id.asc())
            .fetch();

        // 점주와 일반 근무자를 통합하여 반환
        List<UserWorkspaceWorkerListResponse> allWorkers = new ArrayList<>();
        
        // 1. 점주(매니저) 먼저 추가
        managerUsers.stream()
            .map(UserWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        // 2. 일반 근무자(알바생) 나중에 추가
        workspaceWorkers.stream()
            .map(UserWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        return allWorkers;
    }

    @Override
    public List<UserWorkspaceWorkerListResponse> getUserWorkspaceWorkerListWithCursor(
        User user,
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUser qUser = QUser.user;
        QManagerUser qManagerUser = QManagerUser.managerUser;

        // 점주와 일반 근무자를 통합하여 반환
        List<UserWorkspaceWorkerListResponse> allWorkers = new ArrayList<>();
        
        // 1. ManagerUser 조회 (점주) - 우선 조회
        List<ManagerUser> managerUsers = queryFactory
            .selectFrom(qManagerUser)
            .join(qManagerUser.user, qUser).fetchJoin()
            .join(qManagerUser.workspaces, qWorkspace)
            .where(
                qWorkspace.id.eq(workspaceId)
            )
            .orderBy(qUser.name.asc(), qManagerUser.id.asc())
            .fetch();

        // 2. WorkspaceWorker 조회 (알바생) - 커서 조건 적용
        List<WorkspaceWorker> workspaceWorkers = queryFactory
            .selectFrom(qWorkspaceWorker)
            .join(qWorkspaceWorker.workspace, qWorkspace).fetchJoin()
            .join(qWorkspaceWorker.user, qUser).fetchJoin()
            .where(
                qWorkspace.id.eq(workspaceId),
                cursorConditions(qWorkspaceWorker, pageRequest.cursor())
            )
            .orderBy(qUser.name.asc(), qWorkspaceWorker.id.asc())
            .limit(pageRequest.pageSize())
            .fetch();

        // 3. 점주 먼저 추가, 알바생 나중에 추가
        managerUsers.stream()
            .map(UserWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        workspaceWorkers.stream()
            .map(UserWorkspaceWorkerListResponse::from)
            .forEach(allWorkers::add);
        
        return allWorkers;
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

}