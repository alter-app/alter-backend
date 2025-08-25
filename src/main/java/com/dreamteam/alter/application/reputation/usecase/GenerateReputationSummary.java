package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordFrequency;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordSummaryDto;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryBatchData;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("generateReputationSummary")
@RequiredArgsConstructor
@Transactional
public class GenerateReputationSummary implements GenerateReputationSummaryUseCase {

    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;
    private final ReputationSummaryRepository reputationSummaryRepository;

    @Override
    public void execute(ReputationType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return;
        }

        log.info("평판 요약 생성 시작 - 타입: {}, 대상 수: {}", targetType, targetIds.size());

        List<ReputationSummaryBatchData> batchDataList = reputationSummaryQueryRepository
            .getReputationSummaryBatchData(targetType, targetIds);

        List<ReputationSummary> summariesToSave = new ArrayList<>();
        List<ReputationSummary> summariesToUpdate = new ArrayList<>();

        for (ReputationSummaryBatchData batchData : batchDataList) {
            if (!batchData.hasKeywordData()) {
                log.info("평판 데이터 없음 - 타입: {}, 대상: {}", targetType, batchData.getTargetId());
                continue;
            }

            // 키워드별 퍼센티지 계산
            List<KeywordFrequency> keywordFrequenciesWithPercentage = batchData.getKeywordFrequencies().stream()
                .map(kf -> {
                    Double percentage = batchData.getTotalReputationCount() > 0 ? 
                        (kf.getCount() * 100.0 / batchData.getTotalReputationCount()) : 0.0;
                    return KeywordFrequency.of(
                        kf.getKeywordId(),
                        kf.getKeywordName(),
                        kf.getKeywordDescription(),
                        kf.getCount(),
                        percentage,
                        kf.getUserDescriptions()
                    );
                })
                .toList();

            // AI 요약을 위한 입력 데이터 구성
            ReputationSummaryData aiInputData = ReputationSummaryData.of(
                targetType,
                batchData.getTotalReputationCount(),
                keywordFrequenciesWithPercentage
            );

            // AI를 통한 요약 생성 (임시로 null)
            String aiGeneratedSummary = null;

            // KeywordSummaryDto 리스트 생성
            List<KeywordSummaryDto> topKeywords = keywordFrequenciesWithPercentage.stream()
                .map(kf -> KeywordSummaryDto.of(
                    kf.getKeywordId(),
                    kf.getKeywordName(),
                    kf.getKeywordDescription(),
                    kf.getCount()
                ))
                .toList();

            if (batchData.hasExistingSummary()) {
                // 기존 요약 업데이트
                batchData.getExistingSummary().updateSummary(
                    batchData.getTotalReputationCount(), 
                    topKeywords, 
                    aiGeneratedSummary
                );
                summariesToUpdate.add(batchData.getExistingSummary());
            } else {
                // 새 요약 생성
                ReputationSummary newSummary = ReputationSummary.create(
                    targetType, 
                    batchData.getTargetId(), 
                    batchData.getTotalReputationCount(), 
                    topKeywords, 
                    aiGeneratedSummary
                );
                summariesToSave.add(newSummary);
            }
        }

        // 배치 저장/업데이트
        if (!summariesToSave.isEmpty()) {
            reputationSummaryRepository.saveAll(summariesToSave);
        }

        log.info("평판 요약 처리 완료 - 타입: {}, 생성: {}, 업데이트: {}", 
            targetType, summariesToSave.size(), summariesToUpdate.size());
    }
}
