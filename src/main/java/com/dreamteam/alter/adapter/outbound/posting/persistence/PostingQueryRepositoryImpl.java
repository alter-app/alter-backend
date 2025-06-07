package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.domain.posting.entity.*;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostingQueryRepositoryImpl implements PostingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long getCountOfPostings() {
        QPosting qPosting = QPosting.posting;

        Long count = queryFactory
            .select(qPosting.count())
            .from(qPosting)
            .where(qPosting.status.eq(PostingStatus.OPEN))
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;

        List<Long> postingIds = queryFactory
            .select(qPosting.id)
            .from(qPosting)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                cursorConditions(qPosting, request.cursor())
            )
            .orderBy(qPosting.createdAt.desc(), qPosting.id.desc())
            .limit(request.pageSize())
            .fetch();

        if (ObjectUtils.isEmpty(postingIds)) {
            return Collections.emptyList();
        }

        List<Posting> postings = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .where(qPosting.id.in(postingIds))
            .orderBy(qPosting.createdAt.desc(), qPosting.id.desc())
            .distinct()
            .fetch();

        List<PostingKeywordMap> keywordMaps = queryFactory
            .selectFrom(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword).fetchJoin()
            .where(qPostingKeywordMap.posting.id.in(postingIds))
            .fetch();

        Map<Long, List<PostingKeyword>> postingIdToKeywords = keywordMaps.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    pkMap -> pkMap.getPosting().getId(),
                    java.util.stream.Collectors.mapping(PostingKeywordMap::getPostingKeyword, java.util.stream.Collectors.toList())
                )
            );

        return postings.stream()
            .map(
                posting -> PostingListResponse.of(posting, postingIdToKeywords)
            )
            .toList();
    }

    @Override
    public PostingDetailResponse getPostingDetail(Long postingId) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;

        Posting posting = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .where(qPosting.id.eq(postingId))
            .fetchOne();

        if (ObjectUtils.isEmpty(posting)) {
            return null;
        }

        List<PostingKeyword> postingKeywords = queryFactory
            .select(qPostingKeyword)
            .from(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword)
            .where(qPostingKeywordMap.posting.id.eq(postingId))
            .fetch();

        return PostingDetailResponse.of(posting, postingKeywords);
    }

    @Override
    public Optional<Posting> findById(Long postingId) {
        QPosting qPosting = QPosting.posting;

        Posting posting = queryFactory
            .select(qPosting)
            .from(qPosting)
            .where(
                qPosting.id.eq(postingId),
                qPosting.status.eq(PostingStatus.OPEN)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(posting) ? Optional.empty() : Optional.of(posting);
    }

    private BooleanExpression cursorConditions(QPosting qPosting, CursorDto cursor) {
        return ObjectUtils.isEmpty(cursor)
            ? null
            : ltCreatedAt(qPosting, cursor.getCreatedAt())
                .or(eqCreatedAt(qPosting, cursor.getCreatedAt())
                    .and(ltPostingId(qPosting, cursor.getId())));
    }

    private BooleanExpression ltCreatedAt(QPosting qPosting, LocalDateTime createdAt) {
        return createdAt != null ? qPosting.createdAt.lt(createdAt) : null;
    }

    private BooleanExpression eqCreatedAt(QPosting qPosting, LocalDateTime createdAt) {
        return createdAt != null ? qPosting.createdAt.eq(createdAt) : null;
    }

    private BooleanExpression ltPostingId(QPosting qPosting, Long postingId) {
        return postingId != null ? qPosting.id.lt(postingId) : null;
    }

}
