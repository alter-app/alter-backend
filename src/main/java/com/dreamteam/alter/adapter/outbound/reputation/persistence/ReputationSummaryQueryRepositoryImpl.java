package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordFrequency;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.querydsl.core.Tuple;

import static com.dreamteam.alter.domain.reputation.entity.QReputationSummary.reputationSummary;
import static com.dreamteam.alter.domain.reputation.entity.QReputation.reputation;
import static com.dreamteam.alter.domain.reputation.entity.QReputationKeyword.reputationKeyword;
import static com.dreamteam.alter.domain.reputation.entity.QReputationKeywordMap.reputationKeywordMap;

@Repository
@RequiredArgsConstructor
public class ReputationSummaryQueryRepositoryImpl implements ReputationSummaryQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Optional<ReputationSummary> findByTarget(ReputationType targetType, Long targetId) {
        ReputationSummary result = queryFactory
            .selectFrom(reputationSummary)
            .where(
                reputationSummary.targetType.eq(targetType),
                reputationSummary.targetId.eq(targetId)
            )
            .fetchOne();
        
        return Optional.ofNullable(result);
    }

    @Override
    public List<ReputationSummary> findInactiveSummaries(ReputationType targetType, LocalDateTime inactiveThreshold) {
        return queryFactory
            .selectFrom(reputationSummary)
            .where(
                reputationSummary.targetType.eq(targetType),
                reputationSummary.targetId.notIn(
                    // 최근 활성 대상들은 제외
                    queryFactory
                        .select(reputation.targetId)
                        .from(reputation)
                        .where(
                            reputation.targetType.eq(targetType),
                            reputation.status.eq(ReputationStatus.COMPLETED),
                            reputation.createdAt.goe(inactiveThreshold)
                        )
                        .groupBy(reputation.targetId)
                )
            )
            .fetch();
    }

    @Override
    public List<Long> getActiveReputationTargets(ReputationType targetType, LocalDateTime since) {
        return queryFactory
            .select(reputation.targetId)
            .from(reputation)
            .where(
                reputation.targetType.eq(targetType),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(since)
            )
            .groupBy(reputation.targetId)
            .fetch();
    }

    @Override
    public List<KeywordFrequency> getKeywordFrequenciesForAi(ReputationType targetType, Long targetId) {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // 상위 5개 키워드 ID 조회
        List<String> topKeywordIds = queryFactory
            .select(reputationKeyword.id)
            .from(reputationKeywordMap)
            .join(reputationKeywordMap.reputation, reputation)
            .join(reputationKeywordMap.keyword, reputationKeyword)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.eq(targetId),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .groupBy(reputationKeyword.id)
            .orderBy(reputationKeywordMap.count().desc())
            .limit(5)
            .fetch();

        if (topKeywordIds.isEmpty()) {
            return List.of();
        }

        return topKeywordIds.stream()
            .map(keywordId -> {
                // 키워드 기본 정보 조회
                Tuple keywordInfo = queryFactory
                    .select(
                        reputationKeyword.id,
                        reputationKeyword.emoji,
                        reputationKeyword.description,
                        reputationKeywordMap.count().intValue()
                    )
                    .from(reputationKeywordMap)
                    .join(reputationKeywordMap.reputation, reputation)
                    .join(reputationKeywordMap.keyword, reputationKeyword)
                    .where(
                        reputation.targetType.eq(targetType),
                        reputation.targetId.eq(targetId),
                        reputation.status.eq(ReputationStatus.COMPLETED),
                        reputation.createdAt.goe(oneYearAgo),
                        reputationKeyword.id.eq(keywordId)
                    )
                    .groupBy(reputationKeyword.id, reputationKeyword.emoji, reputationKeyword.description)
                    .fetchOne();

                // 해당 키워드의 사용자 설명들 조회
                List<String> userDescriptions = queryFactory
                    .select(reputationKeywordMap.description)
                    .from(reputationKeywordMap)
                    .join(reputationKeywordMap.reputation, reputation)
                    .join(reputationKeywordMap.keyword, reputationKeyword)
                    .where(
                        reputation.targetType.eq(targetType),
                        reputation.targetId.eq(targetId),
                        reputation.status.eq(ReputationStatus.COMPLETED),
                        reputation.createdAt.goe(oneYearAgo),
                        reputationKeyword.id.eq(keywordId),
                        reputationKeywordMap.description.isNotNull()
                    )
                    .fetch()
                    .stream()
                    .filter(desc -> desc != null && !desc.trim().isEmpty())
                    .toList();

                Integer count = keywordInfo.get(3, Integer.class);
                Integer totalCount = queryFactory
                    .select(reputation.count().intValue())
                    .from(reputation)
                    .where(
                        reputation.targetType.eq(targetType),
                        reputation.targetId.eq(targetId),
                        reputation.status.eq(ReputationStatus.COMPLETED),
                        reputation.createdAt.goe(oneYearAgo)
                    )
                    .fetchOne();

                Double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;

                return KeywordFrequency.of(
                    keywordInfo.get(reputationKeyword.id),
                    keywordInfo.get(reputationKeyword.emoji),
                    keywordInfo.get(reputationKeyword.description),
                    count,
                    percentage,
                    userDescriptions
                );
            })
            .toList();
    }

    @Override
    public ReputationSummaryData getReputationSummaryData(ReputationType targetType, Long targetId) {
        // AI 요약에 필요한 최소한의 추가 정보만 조회
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        
        // 총 평판 개수 조회
        Integer totalReputationCount = queryFactory
            .select(reputation.count().intValue())
            .from(reputation)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.eq(targetId),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .fetchOne();
            
        if (ObjectUtils.isEmpty(totalReputationCount) || totalReputationCount == 0) {
            return ReputationSummaryData.of(
                targetType,
                0,
                null,
                null
            );
        }
        
        // 평판 작성자 타입 분포 조회 (AI 컨텍스트용)
        List<Tuple> writerTypeResults = queryFactory
            .select(reputation.writerType, reputation.count().intValue())
            .from(reputation)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.eq(targetId),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .groupBy(reputation.writerType)
            .fetch();
            
        Map<ReputationType, Integer> writerTypeDistribution = writerTypeResults.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(reputation.writerType),
                tuple -> tuple.get(1, Integer.class)
            ));
        
        return ReputationSummaryData.of(
            targetType,
            totalReputationCount,
            null, // topKeywords는 별도로 처리
            writerTypeDistribution
        );
    }
}
