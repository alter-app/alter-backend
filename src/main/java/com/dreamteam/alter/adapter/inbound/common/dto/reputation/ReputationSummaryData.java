package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ReputationSummaryData {
    
    /**
     * 대상 타입 (USER 또는 WORKSPACE)
     */
    private ReputationType targetType;
    
    /**
     * 총 평판 개수
     */
    private Integer totalReputationCount;
    
    /**
     * 상위 키워드 목록 (빈도순, 사용자 설명 포함)
     */
    private List<KeywordFrequency> topKeywords;
    
    /**
     * AI 요약을 위한 팩토리 메서드
     */
    public static ReputationSummaryData of(
        ReputationType targetType,
        Integer totalReputationCount,
        List<KeywordFrequency> topKeywords
    ) {
        return ReputationSummaryData.builder()
            .targetType(targetType)
            .totalReputationCount(totalReputationCount)
            .topKeywords(topKeywords)
            .build();
    }
}

