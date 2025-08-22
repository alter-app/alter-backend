package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordFrequency;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordSummaryDto;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryUseCase;

import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service("generateReputationSummary")
@RequiredArgsConstructor
@Transactional
public class GenerateReputationSummary implements GenerateReputationSummaryUseCase {


    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;
    private final ReputationSummaryRepository reputationSummaryRepository;
//    private final ReputationSummaryService reputationSummaryService;

    @Override
    public void execute(ReputationType targetType, Long targetId) {
        // AI 요약을 위한 상세 키워드 정보 조회 (사용자 설명 포함)
        List<KeywordFrequency> keywordFrequencies = reputationSummaryQueryRepository
            .getKeywordFrequenciesForAi(targetType, targetId);

        if (keywordFrequencies.isEmpty()) {
            log.debug("No reputation data found for {}:{}", targetType, targetId);
            return;
        }

        // 총 평판 개수 계산
        Integer totalCount = keywordFrequencies.stream()
            .mapToInt(KeywordFrequency::getCount)
            .sum();

        // AI 요약을 위한 추가 컨텍스트 조회 (작성자 분포 등)
        ReputationSummaryData summaryData = reputationSummaryQueryRepository
            .getReputationSummaryData(targetType, targetId);
            
        ReputationSummaryData aiInputData = ReputationSummaryData.of(
            targetType,
            totalCount,
            keywordFrequencies,
            summaryData.getWriterTypeDistribution()
        );

        // AI를 통한 요약 생성
//        String aiGeneratedSummary = reputationSummaryService.generateSummary(aiInputData);
        String aiGeneratedSummary = null; // temporary

        // KeywordSummaryDto 리스트 생성
        List<KeywordSummaryDto> topKeywords = keywordFrequencies.stream()
            .map(kf -> KeywordSummaryDto.of(
                kf.getKeywordId(),
                kf.getKeywordName(),
                kf.getKeywordDescription(),
                kf.getCount()
            ))
            .toList();

        Optional<ReputationSummary> existingSummary = reputationSummaryQueryRepository
            .findByTarget(targetType, targetId);

        if (existingSummary.isPresent()) {
            existingSummary.get().updateSummary(
                totalCount,
                topKeywords, 
                aiGeneratedSummary
            );
            log.debug("Updated reputation summary for {}:{}", targetType, targetId);
        } else {
            // 새 요약 생성
            ReputationSummary newSummary = ReputationSummary.create(
                targetType, targetId, totalCount, topKeywords, aiGeneratedSummary
            );
            reputationSummaryRepository.save(newSummary);
            log.debug("Created new reputation summary for {}:{}", targetType, targetId);
        }
    }
}
