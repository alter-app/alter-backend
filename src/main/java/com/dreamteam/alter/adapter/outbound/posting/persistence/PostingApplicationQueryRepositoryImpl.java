package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
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
import com.dreamteam.alter.domain.reputation.entity.QReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

@Repository
@RequiredArgsConstructor
public class PostingApplicationQueryRepositoryImpl implements PostingApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long getCountByUser(User user) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;

        Long count = queryFactory
            .select(qPostingApplication.count())
            .from(qPostingApplication)
            .where(
                qPostingApplication.user.eq(user),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserPostingApplicationListResponse> getUserPostingApplicationList(
        User user,
        PageRequestDto pageRequest
    ) {
        QPostingApplication qPostingApplication = QPostingApplication.postingApplication;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

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
            .where(
                qPostingApplication.user.eq(user),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
            )
            .orderBy(qPostingApplication.createdAt.desc(), qPostingApplication.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getLimit())
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
            .join(qWorkspace.managerUser, QManagerUser.managerUser)
            .where(
                qManagerUser.eq(managerUser),
                eqWorkspaceId(qWorkspace, filter.getWorkspaceId()),
                eqApplicationStatusOrDefault(qPostingApplication, filter.getStatus()),
                eqApplicationStatus(qPostingApplication, filter.getStatus()),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
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
                qManagerUser.eq(managerUser),
                eqWorkspaceId(qWorkspace, filter.getWorkspaceId()),
                eqApplicationStatusOrDefault(qPostingApplication, filter.getStatus()),
                cursorConditions(qPostingApplication, request.cursor()),
                qPostingApplication.status.ne(PostingApplicationStatus.DELETED)
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

    private BooleanExpression eqApplicationStatus(QPostingApplication qPostingApplication, PostingApplicationStatus status) {
        return status != null ? qPostingApplication.status.eq(status) : null;
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

    private BooleanExpression eqApplicationStatusOrDefault(QPostingApplication qPostingApplication, PostingApplicationStatus status) {
        if (ObjectUtils.isNotEmpty(status)) {
            return qPostingApplication.status.eq(status);
        }
        return qPostingApplication.status.in(PostingApplicationStatus.defaultInquirableStatuses());
    }

}
