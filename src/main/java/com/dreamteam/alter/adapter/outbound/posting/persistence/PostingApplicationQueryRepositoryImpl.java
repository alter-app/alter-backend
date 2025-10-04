package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserPostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingApplicationWorkspaceResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.QPosting;
import com.dreamteam.alter.domain.posting.entity.QPostingApplication;
import com.dreamteam.alter.domain.posting.entity.QPostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.entity.*;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.dreamteam.alter.domain.reputation.entity.QReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

@Repository
@RequiredArgsConstructor
public class PostingApplicationQueryRepositoryImpl implements PostingApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long getCountByUser(User user, UserPostingApplicationListFilterDto filter) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;

        BooleanExpression whereCondition = buildUserBaseConditions(qPostingApplication, user, filter);

        Long count = queryFactory
            .select(qPostingApplication.count())
            .from(qPostingApplication)
            .where(whereCondition)
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserPostingApplicationListResponse> getUserPostingApplicationListWithCursor(
        User user,
        CursorPageRequest<CursorDto> pageRequest,
        UserPostingApplicationListFilterDto filter
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        BooleanExpression whereCondition = buildUserBaseConditions(qPostingApplication, user, filter);

        if (pageRequest.cursor() != null) {
            whereCondition = whereCondition.and(
                qPostingApplication.createdAt.lt(pageRequest.cursor().getCreatedAt())
                .or(
                    qPostingApplication.createdAt.eq(pageRequest.cursor().getCreatedAt())
                    .and(qPostingApplication.id.lt(pageRequest.cursor().getId()))
                )
            );
        }

        return queryFactory
            .select(Projections.constructor(
                UserPostingApplicationListResponse.class,
                qPostingApplication.id,
                qPostingSchedule,
                qPosting,
                qPostingApplication.description,
                qPostingApplication.status,
                qPostingApplication.createdAt,
                qPostingApplication.updatedAt
            ))
            .from(qPostingApplication)
            .join(qPostingApplication.postingSchedule.posting, qPosting)
            .join(qPostingApplication.postingSchedule, qPostingSchedule)
            .join(qPosting.workspace, qWorkspace).fetchJoin()
            .where(whereCondition)
            .orderBy(qPostingApplication.createdAt.desc(), qPostingApplication.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public Optional<PostingApplication> getUserPostingApplication(
        User user,
        Long applicationId
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;

        PostingApplication result = queryFactory
            .select(qPostingApplication)
            .from(qPostingApplication)
            .where(
                qPostingApplication.id.eq(applicationId),
                qPostingApplication.user.eq(user),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(result) ? Optional.empty() : Optional.of(result);
    }

    @Override
    public long getManagerPostingApplicationCount(
        ManagerUser managerUser,
        PostingApplicationListFilterDto filter
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QManagerUser qManagerUser = QManagerUser.managerUser;

        Long count = queryFactory
            .select(qPostingApplication.count())
            .from(qPostingApplication)
            .join(qPostingApplication.postingSchedule, qSchedule)
            .join(qSchedule.posting, qPosting)
            .join(qPosting.workspace, qWorkspace)
            .join(qWorkspace.managerUser, qManagerUser)
            .where(
                getManagerPostingApplicationBaseConditions(qManagerUser, managerUser, qWorkspace, filter),
                eqApplicationStatusOrDefault(qPostingApplication, filter.getStatus())
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ManagerPostingApplicationListResponse> getManagerPostingApplicationListWithCursor(
        ManagerUser managerUser,
        CursorPageRequest<CursorDto> request,
        PostingApplicationListFilterDto filter
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QUser qUser = QUser.user;
        QReputationSummary qReputationSummary = QReputationSummary.reputationSummary;

        return queryFactory
            .select(Projections.constructor(
                ManagerPostingApplicationListResponse.class,
                qPostingApplication.id,
                Projections.constructor(
                    PostingApplicationWorkspaceResponse.class,
                    qWorkspace.id,
                    qWorkspace.businessName
                ),
                qPostingSchedule,
                qPostingApplication.status,
                qPostingApplication.user,
                qPostingApplication.createdAt,
                qReputationSummary
            ))
            .from(qPostingApplication)
            .join(qPostingApplication.postingSchedule, qPostingSchedule)
            .join(qPostingSchedule.posting, qPosting)
            .join(qPosting.workspace, qWorkspace)
            .join(qWorkspace.managerUser, qManagerUser)
            .join(qPostingApplication.user, qUser)
            .leftJoin(qReputationSummary)
            .on(
                qReputationSummary.targetType.eq(ReputationType.USER),
                qReputationSummary.targetId.eq(qUser.id)
            )
            .where(
                getManagerPostingApplicationBaseConditions(qManagerUser, managerUser, qWorkspace, filter),
                eqApplicationStatusOrDefault(qPostingApplication, filter.getStatus()),
                cursorConditions(qPostingApplication, request.cursor())
            )
            .orderBy(qPostingApplication.createdAt.desc(), qPostingApplication.id.desc())
            .limit(request.pageSize())
            .fetch();
    }

    @Override
    public Optional<ManagerPostingApplicationDetailResponse> getManagerPostingApplicationDetail(
        ManagerUser managerUser,
        Long postingApplicationId
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QManagerUser qManagerUser = QManagerUser.managerUser;
        QUser qUser = QUser.user;

        return Optional.ofNullable(
            queryFactory
            .select(Projections.constructor(
                ManagerPostingApplicationDetailResponse.class,
                qPostingApplication.id,
                Projections.constructor(
                    PostingApplicationWorkspaceResponse.class,
                    qWorkspace.id,
                    qWorkspace.businessName
                ),
                qPostingSchedule,
                qPostingApplication.description,
                qPostingApplication.status,
                qPostingApplication.user,
                qPostingApplication.createdAt
            )).distinct()
            .from(qPostingApplication)
            .join(qPostingApplication.postingSchedule, qPostingSchedule)
            .join(qPostingSchedule.posting, qPosting)
            .join(qPosting.workspace, qWorkspace)
            .join(qWorkspace.managerUser, qManagerUser)
            .join(qPostingApplication.user, qUser)
            .where(
                qManagerUser.eq(managerUser),
                qPostingApplication.id.eq(postingApplicationId),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
            )
            .fetchOne()
        );
    }

    @Override
    public Optional<PostingApplication> getByManagerAndId(ManagerUser managerUser, Long postingApplicationId) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QManagerUser qManagerUser = QManagerUser.managerUser;

        return Optional.ofNullable(
            queryFactory
                .select(qPostingApplication)
                .from(qPostingApplication)
                .join(qPostingApplication.postingSchedule, qPostingSchedule)
                .join(qPostingSchedule.posting, qPosting)
                .join(qPosting.workspace, qWorkspace)
                .join(qWorkspace.managerUser, qManagerUser)
                .where(
                    qManagerUser.eq(managerUser),
                    qPostingApplication.id.eq(postingApplicationId),
                    qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
                )
                .fetchOne()
        );
    }

    private BooleanExpression eqWorkspaceId(QWorkspace qWorkspace, Long workspaceId) {
        return workspaceId != null ? qWorkspace.id.eq(workspaceId) : null;
    }

    private BooleanExpression cursorConditions(QPostingApplication qPostingApplication, CursorDto cursor) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }
        LocalDateTime createdAt = cursor.getCreatedAt();
        Long id = cursor.getId();
        return (createdAt != null && id != null)
            ? qPostingApplication.createdAt.lt(createdAt)
                .or(qPostingApplication.createdAt.eq(createdAt).and(qPostingApplication.id.lt(id)))
            : null;
    }

    private BooleanExpression eqApplicationStatusOrDefault(QPostingApplication qPostingApplication, Set<PostingApplicationStatus> statuses) {
        BooleanExpression statusCondition;
        if (ObjectUtils.isNotEmpty(statuses)) {
            statusCondition = qPostingApplication.status.in(statuses);
        } else {
            statusCondition = null;
        }
        
        // DELETED 상태는 항상 제외
        return statusCondition != null 
            ? statusCondition.and(qPostingApplication.status.ne(PostingApplicationStatus.DELETED))
            : qPostingApplication.status.ne(PostingApplicationStatus.DELETED);
    }

    private BooleanExpression buildUserBaseConditions(
        QPostingApplication qPostingApplication,
        User user,
        UserPostingApplicationListFilterDto filter
    ) {
        BooleanExpression whereCondition = qPostingApplication.user.eq(user)
            .and(qPostingApplication.status.ne(PostingApplicationStatus.DELETED));

        if (ObjectUtils.isNotEmpty(filter) && ObjectUtils.isNotEmpty(filter.getStatus())) {
            whereCondition = whereCondition.and(qPostingApplication.status.in(filter.getStatus()));
        }

        return whereCondition;
    }

    private BooleanExpression getManagerPostingApplicationBaseConditions(
        QManagerUser qManagerUser,
        ManagerUser managerUser,
        QWorkspace qWorkspace,
        PostingApplicationListFilterDto filter
    ) {
        BooleanExpression managerCondition = qManagerUser.eq(managerUser);
        BooleanExpression workspaceCondition = eqWorkspaceId(qWorkspace, filter.getWorkspaceId());
        
        if (ObjectUtils.isNotEmpty(workspaceCondition)) {
            return managerCondition.and(workspaceCondition);
        }
        return managerCondition;
    }

}
