package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordFrequency;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface ReputationSummaryQueryRepository {

    Optional<ReputationSummary> findByTarget(ReputationType targetType, Long targetId);
    
    /**
     * 기간 만료 평판 요약 조회 (마지막 평판 수신 시점이 임계값 이전)
     */
    List<ReputationSummary> findInactiveSummaries(ReputationType targetType, LocalDateTime inactiveThreshold);

    /**
     * 특정 시점 이후에 평판을 받은 활성 대상 ID 목록 조회
     */
    List<Long> getActiveReputationTargets(ReputationType targetType, LocalDateTime since);
    
    /**
     * AI 요약을 위한 키워드 상세 정보 조회
     */
    List<KeywordFrequency> getKeywordFrequenciesForAi(ReputationType targetType, Long targetId);
    
    /**
     * AI 요약을 위한 상세 평판 데이터 조회
     */
    ReputationSummaryData getReputationSummaryData(ReputationType targetType, Long targetId);
}
