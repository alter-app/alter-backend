package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.QPosting;
import com.dreamteam.alter.domain.posting.entity.QPostingApplication;
import com.dreamteam.alter.domain.posting.entity.QPostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
            .where(qPostingApplication.user.eq(user))
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
            .where(qPostingApplication.user.eq(user))
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
                qPostingApplication.user.eq(user)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(result) ? Optional.empty() : Optional.of(result);
    }

}
