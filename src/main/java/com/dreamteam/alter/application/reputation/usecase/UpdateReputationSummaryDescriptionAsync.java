package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryTextUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.UpdateReputationSummaryDescriptionAsyncUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("updateReputationSummaryDescriptionAsync")
@RequiredArgsConstructor
public class UpdateReputationSummaryDescriptionAsync implements UpdateReputationSummaryDescriptionAsyncUseCase {

    private final GenerateReputationSummaryTextUseCase generateReputationSummaryText;
    private final ReputationSummaryRepository reputationSummaryRepository;

    @Override
    @Async("batchTaskExecutor")
    @Transactional
    public void execute(ReputationSummary reputationSummary, ReputationSummaryData summaryData) {
        log.info(
            "평판 요약 설명 업데이트 - 타입: {}, ID: {}",
            reputationSummary.getTargetType(), reputationSummary.getTargetId()
        );

        // AI를 통한 요약 텍스트 생성
        String aiGeneratedSummary = generateReputationSummaryText.execute(summaryData);

        if (ObjectUtils.isNotEmpty(aiGeneratedSummary) && !aiGeneratedSummary.trim().isEmpty()) {
            // 요약 내용 업데이트
            reputationSummary.updateSummaryDescription(aiGeneratedSummary);
            reputationSummaryRepository.save(reputationSummary);
        } else {
            log.warn(
                "평판 요약 설명 생성 실패 - 타입: {}, ID: {}",
                reputationSummary.getTargetType(), reputationSummary.getTargetId()
            );
        }
    }
}
