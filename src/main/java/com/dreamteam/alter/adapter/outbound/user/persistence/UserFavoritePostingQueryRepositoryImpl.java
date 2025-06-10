package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFavoritePostingListResponse;
import com.dreamteam.alter.domain.posting.entity.QPosting;
import com.dreamteam.alter.domain.user.entity.QUserFavoritePosting;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingQueryRepository;
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

@Repository
@RequiredArgsConstructor
public class UserFavoritePostingQueryRepositoryImpl implements UserFavoritePostingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long getCountByUser(User user) {
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        Long count = queryFactory
            .select(qUserFavoritePosting.count())
            .from(qUserFavoritePosting)
            .where(qUserFavoritePosting.user.eq(user))
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<UserFavoritePostingListResponse> findUserFavoritePostingListWithCursor(
        User user,
        CursorPageRequest<CursorDto> request
    ) {
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        return queryFactory
            .select(Projections.constructor(
                UserFavoritePostingListResponse.class,
                qUserFavoritePosting.id,
                qUserFavoritePosting.posting,
                qUserFavoritePosting.createdAt
            ))
            .from(qUserFavoritePosting)
            .join(qUserFavoritePosting.posting, qPosting)
            .join(qPosting.workspace, qWorkspace)
            .fetchJoin()
            .where(
                qUserFavoritePosting.user.eq(user),
                cursorConditions(qUserFavoritePosting, request.cursor())
            )
            .orderBy(qUserFavoritePosting.createdAt.desc(), qUserFavoritePosting.id.desc())
            .limit(request.pageSize())
            .fetch();
    }

    @Override
    public Optional<UserFavoritePosting> findByPostingIdAndUser(Long postingId, User user) {
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        return Optional.ofNullable(
            queryFactory
                .select(qUserFavoritePosting)
                .from(qUserFavoritePosting)
                .where(
                    qUserFavoritePosting.posting.id.eq(postingId),
                    qUserFavoritePosting.user.eq(user)
                )
                .fetchOne()
        );
    }

    private BooleanExpression cursorConditions(QUserFavoritePosting qUserFavoritePosting, CursorDto cursor) {
        return ObjectUtils.isEmpty(cursor)
            ? null
            : ltCreatedAt(qUserFavoritePosting, cursor.getCreatedAt())
                .or(eqCreatedAt(qUserFavoritePosting, cursor.getCreatedAt())
                    .and(ltUserFavoritePostingId(qUserFavoritePosting, cursor.getId())));
    }

    private BooleanExpression ltCreatedAt(QUserFavoritePosting qUserFavoritePosting, LocalDateTime createdAt) {
        return createdAt != null ? qUserFavoritePosting.createdAt.lt(createdAt) : null;
    }

    private BooleanExpression eqCreatedAt(QUserFavoritePosting qUserFavoritePosting, LocalDateTime createdAt) {
        return createdAt != null ? qUserFavoritePosting.createdAt.eq(createdAt) : null;
    }

    private BooleanExpression ltUserFavoritePostingId(QUserFavoritePosting qUserFavoritePosting, Long favoritePostingId) {
        return favoritePostingId != null ? qUserFavoritePosting.id.lt(favoritePostingId) : null;
    }

}
