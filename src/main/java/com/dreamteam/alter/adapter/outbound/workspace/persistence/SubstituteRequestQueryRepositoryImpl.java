package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ReceivedSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestDetailResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SubstituteRequestTargetInfo;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.QSubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.LockModeType;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.dreamteam.alter.domain.workspace.entity.QSubstituteRequest.substituteRequest;
import static com.dreamteam.alter.domain.workspace.entity.QWorkspaceShift.workspaceShift;
import static com.dreamteam.alter.domain.workspace.entity.QWorkspace.workspace;
import static com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker.workspaceWorker;

@Repository
@RequiredArgsConstructor
public class SubstituteRequestQueryRepositoryImpl implements SubstituteRequestQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SubstituteRequest> findById(Long id) {
        SubstituteRequest result = queryFactory
            .selectFrom(substituteRequest)
            .where(substituteRequest.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<SubstituteRequest> findByIdWithPessimisticLock(Long id) {
        SubstituteRequest result = queryFactory
            .selectFrom(substituteRequest)
            .where(substituteRequest.id.eq(id))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsActiveRequestByScheduleAndRequester(Long scheduleId, Long requesterId) {
        return queryFactory
            .select(substituteRequest.id)
            .from(substituteRequest)
            .where(
                substituteRequest.workspaceShift.id.eq(scheduleId),
                substituteRequest.requesterId.eq(requesterId),
                substituteRequest.status.in(SubstituteRequestStatus.PENDING, SubstituteRequestStatus.ACCEPTED)
            )
            .limit(1)
            .fetchFirst() != null;
    }

    @Override
    public List<SubstituteRequest> findAllByStatusAndExpiresAtBefore(SubstituteRequestStatus status, LocalDateTime expiresAt) {
        return queryFactory
            .selectFrom(substituteRequest)
            .where(
                substituteRequest.status.eq(status),
                substituteRequest.expiresAt.before(expiresAt)
            )
            .fetch();
    }

    @Override
    public long getReceivedRequestCount(User user, Long workspaceId) {
        // 내가 받은 대타 요청: (requestType = ALL OR targetId = 내 ID) AND workspaceId 일치 AND status = PENDING

        Long count = queryFactory
            .select(substituteRequest.count())
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .join(workspaceShift.workspace, workspace)
            .join(workspaceWorker).on(
                workspaceWorker.workspace.id.eq(workspaceId)
                    .and(workspaceWorker.user.eq(user))
            )
            .where(
                workspace.id.eq(workspaceId)
                    .and(substituteRequest.status.eq(SubstituteRequestStatus.PENDING))
                    .and(
                        substituteRequest.requestType.eq(SubstituteRequestType.ALL)
                            .or(JPAExpressions.selectFrom(QSubstituteRequestTarget.substituteRequestTarget)
                                .where(QSubstituteRequestTarget.substituteRequestTarget.substituteRequest.eq(substituteRequest)
                                    .and(QSubstituteRequestTarget.substituteRequestTarget.targetWorkerId.eq(workspaceWorker.id)))
                                .exists())
                    )
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<ReceivedSubstituteRequestListResponse> getReceivedRequestListWithCursor(
        User user,
        Long workspaceId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QUser requesterUser = new QUser("requesterUser");
        QUser acceptedUser = new QUser("acceptedUser");
        QWorkspaceWorker requesterWorker = new QWorkspaceWorker("requesterWorker");
        QWorkspaceWorker acceptedWorker = new QWorkspaceWorker("acceptedWorker");
        QWorkspaceWorker myWorker = new QWorkspaceWorker("myWorker");

        return queryFactory
            .select(Projections.constructor(
                ReceivedSubstituteRequestListResponse.class,
                substituteRequest.id,
                workspaceShift.id,
                workspaceShift.startDateTime,
                workspaceShift.endDateTime,
                workspaceShift.position,
                workspace.id,
                workspace.businessName,
                requesterWorker.id,
                requesterUser.name,
                substituteRequest.requestType,
                acceptedWorker.id,
                acceptedUser.name,
                substituteRequest.status,
                substituteRequest.requestReason,
                substituteRequest.createdAt,
                substituteRequest.acceptedAt,
                substituteRequest.processedAt
            ))
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .join(workspaceShift.workspace, workspace)
            .join(requesterWorker).on(requesterWorker.id.eq(substituteRequest.requesterId))
            .join(requesterUser).on(requesterUser.id.eq(requesterWorker.user.id))
            .leftJoin(acceptedWorker).on(acceptedWorker.id.eq(substituteRequest.acceptedWorkerId))
            .leftJoin(acceptedUser).on(acceptedUser.id.eq(acceptedWorker.user.id))
            .join(myWorker).on(
                myWorker.workspace.id.eq(workspaceId)
                    .and(myWorker.user.eq(user))
            )
            .where(
                workspace.id.eq(workspaceId)
                    .and(substituteRequest.status.eq(SubstituteRequestStatus.PENDING))
                    .and(
                        substituteRequest.requestType.eq(SubstituteRequestType.ALL)
                            .or(JPAExpressions.selectFrom(QSubstituteRequestTarget.substituteRequestTarget)
                                .where(QSubstituteRequestTarget.substituteRequestTarget.substituteRequest.eq(substituteRequest)
                                    .and(QSubstituteRequestTarget.substituteRequestTarget.targetWorkerId.eq(myWorker.id)))
                                .exists())
                    )
                    .and(cursorCondition(pageRequest.cursor()))
            )
            .orderBy(substituteRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getSentRequestCount(User user, SubstituteRequestStatus status) {
        QWorkspaceWorker requesterWorker = new QWorkspaceWorker("requesterWorker");

        Long count = queryFactory
            .select(substituteRequest.count())
            .from(substituteRequest)
            .join(requesterWorker).on(requesterWorker.id.eq(substituteRequest.requesterId))
            .where(
                requesterWorker.user.eq(user)
                    .and(statusCondition(status))
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<SentSubstituteRequestListResponse> getSentRequestListWithCursor(
        User user,
        SubstituteRequestStatus status,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QWorkspaceWorker qWorkspaceWorker = QWorkspaceWorker.workspaceWorker;
        
        return queryFactory
            .select(Projections.constructor(
                SentSubstituteRequestListResponse.class,
                substituteRequest.id,
                workspaceShift.id,
                workspaceShift.startDateTime,
                workspaceShift.endDateTime,
                workspaceShift.position,
                workspace.id,
                workspace.businessName,
                substituteRequest.requestType,
                substituteRequest.status,
                substituteRequest.createdAt
            ))
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .join(workspaceShift.workspace, workspace)
            .join(qWorkspaceWorker).on(qWorkspaceWorker.id.eq(substituteRequest.requesterId))
            .where(
                qWorkspaceWorker.user.eq(user)
                    .and(statusCondition(status))
                    .and(cursorCondition(pageRequest.cursor()))
            )
            .orderBy(substituteRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public Optional<SentSubstituteRequestDetailResponse> getSentRequestDetail(User user, Long requestId) {
        // 1. 요청 기본 정보 조회
        QWorkspaceWorker requesterWorker = new QWorkspaceWorker("requesterWorker");
        QUser requesterUser = new QUser("requesterUser");
        QWorkspaceWorker acceptedWorker = new QWorkspaceWorker("acceptedWorker");
        QUser acceptedUser = new QUser("acceptedUser");
        
        SentSubstituteRequestDetailResponse requestInfo = queryFactory
            .select(Projections.bean(
                SentSubstituteRequestDetailResponse.class,
                substituteRequest.id.as("id"),
                workspaceShift.id.as("scheduleId"),
                workspaceShift.startDateTime.as("scheduleStartDateTime"),
                workspaceShift.endDateTime.as("scheduleEndDateTime"),
                workspaceShift.position.as("position"),
                workspace.id.as("workspaceId"),
                workspace.businessName.as("workspaceName"),
                requesterWorker.id.as("requesterId"),
                requesterUser.name.as("requesterName"),
                substituteRequest.requestType.as("requestType"),
                acceptedWorker.id.as("acceptedWorkerId"),
                acceptedUser.name.as("acceptedWorkerName"),
                substituteRequest.status.as("status"),
                substituteRequest.requestReason.as("requestReason"),
                substituteRequest.createdAt.as("createdAt"),
                substituteRequest.acceptedAt.as("acceptedAt"),
                substituteRequest.processedAt.as("processedAt")
            ))
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .join(workspaceShift.workspace, workspace)
            .join(requesterWorker).on(requesterWorker.id.eq(substituteRequest.requesterId))
            .join(requesterUser).on(requesterUser.id.eq(requesterWorker.user.id))
            .leftJoin(acceptedWorker).on(acceptedWorker.id.eq(substituteRequest.acceptedWorkerId))
            .leftJoin(acceptedUser).on(acceptedUser.id.eq(acceptedWorker.user.id))
            .where(
                substituteRequest.id.eq(requestId)
                    .and(requesterWorker.user.eq(user))
            )
            .fetchOne();

        if (ObjectUtils.isEmpty(requestInfo)) {
            return Optional.empty();
        }

        // 2. 대상자 목록 조회
        QUser targetUser = new QUser("targetUser");
        QWorkspaceWorker targetWorker = new QWorkspaceWorker("targetWorker");
        QSubstituteRequestTarget substituteRequestTarget = new QSubstituteRequestTarget("substituteRequestTarget");

        List<SubstituteRequestTargetInfo> targets = queryFactory
            .select(Projections.constructor(
                SubstituteRequestTargetInfo.class,
                targetWorker.id,
                targetUser.name,
                substituteRequestTarget.status,
                substituteRequestTarget.rejectionReason,
                substituteRequestTarget.respondedAt
            ))
            .from(substituteRequestTarget)
            .join(targetWorker).on(targetWorker.id.eq(substituteRequestTarget.targetWorkerId))
            .join(targetUser).on(targetUser.id.eq(targetWorker.user.id))
            .where(substituteRequestTarget.substituteRequest.id.eq(requestId))
            .orderBy(substituteRequestTarget.id.asc())
            .fetch();

        requestInfo.setTargets(targets);
        return Optional.of(requestInfo);
    }

    @Override
    public long getManagerRequestCount(Long workspaceId, SubstituteRequestStatus status) {
        Long count = queryFactory
            .select(substituteRequest.count())
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .where(
                workspaceShift.workspace.id.eq(workspaceId)
                    .and(managerRequestStatusCondition(status))
            )
            .fetchOne();

        return ObjectUtils.isNotEmpty(count) ? count : 0;
    }

    @Override
    public List<ManagerSubstituteRequestListResponse> getManagerRequestListWithCursor(
        Long workspaceId,
        SubstituteRequestStatus status,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QUser requesterUser = new QUser("requesterUser");
        QUser acceptedUser = new QUser("acceptedUser");
        QWorkspaceWorker requesterWorker = new QWorkspaceWorker("requesterWorker");
        QWorkspaceWorker acceptedWorker = new QWorkspaceWorker("acceptedWorker");

        return queryFactory
            .select(Projections.constructor(
                ManagerSubstituteRequestListResponse.class,
                substituteRequest.id,
                workspaceShift.id,
                workspaceShift.startDateTime,
                workspaceShift.endDateTime,
                workspaceShift.position,
                workspace.id,
                workspace.businessName,
                requesterWorker.id,
                requesterUser.name,
                substituteRequest.requestType,
                acceptedWorker.id,
                acceptedUser.name,
                substituteRequest.status,
                substituteRequest.requestReason,
                substituteRequest.createdAt,
                substituteRequest.acceptedAt,
                substituteRequest.processedAt
            ))
            .from(substituteRequest)
            .join(substituteRequest.workspaceShift, workspaceShift)
            .join(workspaceShift.workspace, workspace)
            .join(requesterWorker).on(requesterWorker.id.eq(substituteRequest.requesterId))
            .join(requesterUser).on(requesterUser.id.eq(requesterWorker.user.id))
            .leftJoin(acceptedWorker).on(acceptedWorker.id.eq(substituteRequest.acceptedWorkerId))
            .leftJoin(acceptedUser).on(acceptedUser.id.eq(acceptedWorker.user.id))
            .where(
                workspace.id.eq(workspaceId)
                    .and(managerRequestStatusCondition(status))
                    .and(cursorCondition(pageRequest.cursor()))
            )
            .orderBy(substituteRequest.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression cursorCondition(CursorDto cursor) {
        if (cursor == null || cursor.getId() == null) {
            return null;
        }
        return substituteRequest.id.lt(cursor.getId());
    }

    private BooleanExpression statusCondition(SubstituteRequestStatus status) {
        if (status == null) {
            return null;
        }
        return substituteRequest.status.eq(status);
    }

    private BooleanExpression managerRequestStatusCondition(SubstituteRequestStatus status) {
        if (ObjectUtils.isEmpty(status)) {
            // 상태가 지정되지 않은 경우 모든 조회 가능한 상태 조회
            return substituteRequest.status.in(SubstituteRequestStatus.getManagerViewableStatuses());
        }
        
        // 특정 상태가 지정된 경우 해당 상태만 조회
        return substituteRequest.status.eq(status);
    }
}
