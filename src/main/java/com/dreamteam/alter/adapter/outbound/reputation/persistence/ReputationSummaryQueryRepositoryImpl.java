package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordFrequency;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryBatchData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

import com.querydsl.core.types.dsl.Expressions;
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
                reputation.updatedAt.goe(since)
            )
            .groupBy(reputation.targetId)
            .fetch();
    }

    @Override
    public List<ReputationSummaryBatchData> getReputationSummaryBatchData(ReputationType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // 모든 대상의 상위 키워드와 빈도수 조회
        List<Tuple> allKeywordsWithCount = queryFactory
            .select(
                reputation.targetId,
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
                reputation.targetId.in(targetIds),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .groupBy(reputation.targetId, reputationKeyword.id, reputationKeyword.emoji, reputationKeyword.description)
            .orderBy(reputation.targetId.asc(), reputationKeywordMap.count().desc())
            .fetch();

        // 모든 대상의 사용자 설명 조회
        List<Tuple> allUserDescriptions = queryFactory
            .select(
                reputation.targetId,
                reputationKeyword.id,
                reputationKeywordMap.description
            )
            .from(reputationKeywordMap)
            .join(reputationKeywordMap.reputation, reputation)
            .join(reputationKeywordMap.keyword, reputationKeyword)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.in(targetIds),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo),
                reputationKeywordMap.description.isNotNull()
            )
            .fetch();

        // 모든 대상의 총 평판 개수 조회
        List<Tuple> totalCounts = queryFactory
            .select(
                reputation.targetId,
                Expressions.numberTemplate(Long.class, "count({0})", reputation.id)
            )
            .from(reputation)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.in(targetIds),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .groupBy(reputation.targetId)
            .fetch();

        // 기존 평판 요약 조회
        List<ReputationSummary> existingSummaries = queryFactory
            .selectFrom(reputationSummary)
            .where(
                reputationSummary.targetType.eq(targetType),
                reputationSummary.targetId.in(targetIds)
            )
            .fetch();

        // 데이터 그룹화
        Map<Long, List<Tuple>> targetKeywordsMap = allKeywordsWithCount.stream()
            .filter(tuple -> ObjectUtils.isNotEmpty(tuple.get(reputation.targetId)))
            .collect(Collectors.groupingBy(tuple -> tuple.get(reputation.targetId)));

        Map<Long, Map<String, List<String>>> targetDescriptionsMap = allUserDescriptions.stream()
            .filter(tuple -> ObjectUtils.isNotEmpty(tuple.get(reputation.targetId)) && ObjectUtils.isNotEmpty(tuple.get(reputationKeyword.id)))
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(reputation.targetId),
                Collectors.groupingBy(
                    tuple -> tuple.get(reputationKeyword.id),
                    Collectors.mapping(
                        tuple -> tuple.get(reputationKeywordMap.description),
                        Collectors.filtering(
                            desc -> ObjectUtils.isNotEmpty(desc) && !desc.trim().isEmpty(),
                            Collectors.toList()
                        )
                    )
                )
            ));

        Map<Long, Integer> targetCountsMap = totalCounts.stream()
            .filter(tuple -> ObjectUtils.isNotEmpty(tuple.get(reputation.targetId)))
            .collect(Collectors.toMap(
                tuple -> tuple.get(reputation.targetId),
                tuple -> ObjectUtils.defaultIfNull(tuple.get(1, Long.class), 0L).intValue()
            ));

        Map<Long, ReputationSummary> existingSummariesMap = existingSummaries.stream()
            .collect(Collectors.toMap(
                ReputationSummary::getTargetId,
                summary -> summary
            ));

        // 배치 데이터 구성
        return targetIds.stream()
            .map(targetId -> {
                List<Tuple> keywords = targetKeywordsMap.get(targetId);
                List<KeywordFrequency> keywordFrequencies = List.of();
                
                if (keywords != null && !keywords.isEmpty()) {
                    List<Tuple> top5Keywords = keywords.stream()
                        .limit(5)
                        .toList();

                    Map<String, List<String>> keywordDescriptions = targetDescriptionsMap.getOrDefault(targetId, Map.of());

                    keywordFrequencies = top5Keywords.stream()
                        .filter(tuple -> ObjectUtils.isNotEmpty(tuple.get(reputationKeyword.id)))
                        .map(tuple -> {
                            String keywordId = tuple.get(reputationKeyword.id);
                            Integer countValue = ObjectUtils.defaultIfNull(tuple.get(reputationKeywordMap.count()), 0L).intValue();
                            List<String> userDescriptions = keywordDescriptions.getOrDefault(keywordId, List.of());

                            return KeywordFrequency.of(
                                keywordId,
                                tuple.get(reputationKeyword.emoji),
                                tuple.get(reputationKeyword.description),
                                countValue,
                                0.0, // percentage는 별도 계산
                                userDescriptions
                            );
                        })
                        .toList();
                }

                Integer totalReputationCount = targetCountsMap.getOrDefault(targetId, 0);
                ReputationSummary existingSummary = existingSummariesMap.get(targetId);

                return ReputationSummaryBatchData.of(
                    targetId,
                    keywordFrequencies,
                    totalReputationCount,
                    existingSummary
                );
            })
            .toList();
    }
}
