package com.dreamteam.alter.domain.reputation.port.inbound.service;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;

public interface ReputationSummaryService {
    /**
     * 평판 데이터를 기반으로 AI 요약 생성
     * 
     * @param summaryData 평판 요약 데이터
     * @return AI가 생성한 요약 텍스트
     */
    String generateSummary(ReputationSummaryData summaryData);
}

