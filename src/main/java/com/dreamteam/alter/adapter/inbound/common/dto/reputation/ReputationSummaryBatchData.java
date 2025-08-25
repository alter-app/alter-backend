package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ReputationSummaryBatchData {
    
    private Long targetId;
    private List<KeywordFrequency> keywordFrequencies;
    private Integer totalReputationCount;
    private ReputationSummary existingSummary;
    
    public static ReputationSummaryBatchData of(
        Long targetId,
        List<KeywordFrequency> keywordFrequencies,
        Integer totalReputationCount,
        ReputationSummary existingSummary
    ) {
        return ReputationSummaryBatchData.builder()
            .targetId(targetId)
            .keywordFrequencies(keywordFrequencies)
            .totalReputationCount(totalReputationCount)
            .existingSummary(existingSummary)
            .build();
    }
    
    public boolean hasKeywordData() {
        return ObjectUtils.isNotEmpty(keywordFrequencies) && !keywordFrequencies.isEmpty();
    }
    
    public boolean hasExistingSummary() {
        return ObjectUtils.isNotEmpty(existingSummary);
    }
    
}
