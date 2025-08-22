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
                reputation.updatedAt.goe(since)
            )
            .groupBy(reputation.targetId)
            .fetch();
    }

    @Override
    public Map<Long, List<KeywordFrequency>> getKeywordFrequencies(ReputationType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Map.of();
        }

        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // 모든 대상의 총 평판 개수 조회
        List<Tuple> totalCounts = queryFactory
            .select(
                reputation.targetId,
                reputation.count().intValue()
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

        Map<Long, Integer> targetTotalCountMap = totalCounts.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(reputation.targetId),
                tuple -> tuple.get(1, Integer.class)
            ));

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

        // 대상별로 키워드 데이터 구성
        Map<Long, List<KeywordFrequency>> result = new java.util.HashMap<>();
        
        // 대상별로 키워드 그룹화
        Map<Long, List<Tuple>> targetKeywordsMap = allKeywordsWithCount.stream()
            .collect(Collectors.groupingBy(tuple -> tuple.get(reputation.targetId)));

        // 대상별로 사용자 설명 그룹화
        Map<Long, Map<String, List<String>>> targetDescriptionsMap = allUserDescriptions.stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(reputation.targetId),
                Collectors.groupingBy(
                    tuple -> tuple.get(reputationKeyword.id),
                    Collectors.mapping(
                        tuple -> tuple.get(reputationKeywordMap.description),
                        Collectors.filtering(
                            desc -> desc != null && !desc.trim().isEmpty(),
                            Collectors.toList()
                        )
                    )
                )
            ));

        for (Long targetId : targetIds) {
            List<Tuple> keywords = targetKeywordsMap.get(targetId);
            if (keywords == null || keywords.isEmpty()) {
                result.put(targetId, List.of());
                continue;
            }

            // 상위 5개만 선택
            List<Tuple> top5Keywords = keywords.stream()
                .limit(5)
                .toList();

            Integer totalCount = targetTotalCountMap.getOrDefault(targetId, 0);
            Map<String, List<String>> keywordDescriptions = targetDescriptionsMap.getOrDefault(targetId, Map.of());

            List<KeywordFrequency> keywordFrequencies = top5Keywords.stream()
                .map(tuple -> {
                    String keywordId = tuple.get(reputationKeyword.id);
                    Integer count = tuple.get(4, Integer.class);
                    Double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
                    List<String> userDescriptions = keywordDescriptions.getOrDefault(keywordId, List.of());

                    return KeywordFrequency.of(
                        keywordId,
                        tuple.get(reputationKeyword.emoji),
                        tuple.get(reputationKeyword.description),
                        count,
                        percentage,
                        userDescriptions
                    );
                })
                .toList();

            result.put(targetId, keywordFrequencies);
        }

        return result;
    }

    @Override
    public Map<Long, ReputationSummaryData> getReputationSummaryData(ReputationType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Map.of();
        }

        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // 모든 대상의 총 평판 개수 조회
        List<Tuple> totalCounts = queryFactory
            .select(
                reputation.targetId,
                reputation.count().intValue()
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

        // 모든 대상의 작성자 타입 분포 조회
        List<Tuple> writerTypeResults = queryFactory
            .select(
                reputation.targetId,
                reputation.writerType,
                reputation.count().intValue()
            )
            .from(reputation)
            .where(
                reputation.targetType.eq(targetType),
                reputation.targetId.in(targetIds),
                reputation.status.eq(ReputationStatus.COMPLETED),
                reputation.createdAt.goe(oneYearAgo)
            )
            .groupBy(reputation.targetId, reputation.writerType)
            .fetch();

        // 결과 구성
        Map<Long, ReputationSummaryData> result = new java.util.HashMap<>();
        Map<Long, Integer> totalCountMap = totalCounts.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(reputation.targetId),
                tuple -> tuple.get(1, Integer.class)
            ));

        Map<Long, Map<ReputationType, Integer>> writerTypeDistributionMap = writerTypeResults.stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(reputation.targetId),
                Collectors.toMap(
                    tuple -> tuple.get(reputation.writerType),
                    tuple -> tuple.get(2, Integer.class)
                )
            ));

        for (Long targetId : targetIds) {
            Integer totalCount = totalCountMap.getOrDefault(targetId, 0);
            Map<ReputationType, Integer> writerTypeDistribution = writerTypeDistributionMap.getOrDefault(targetId, Map.of());

            result.put(targetId, ReputationSummaryData.of(
                targetType,
                totalCount,
                null, // topKeywords는 별도로 처리
                writerTypeDistribution
            ));
        }

        return result;
    }

    @Override
    public Map<Long, ReputationSummary> findExistingSummaries(ReputationType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Map.of();
        }

        List<ReputationSummary> summaries = queryFactory
            .selectFrom(reputationSummary)
            .where(
                reputationSummary.targetType.eq(targetType),
                reputationSummary.targetId.in(targetIds)
            )
            .fetch();

        return summaries.stream()
            .collect(Collectors.toMap(
                ReputationSummary::getTargetId,
                summary -> summary
            ));
    }
}
