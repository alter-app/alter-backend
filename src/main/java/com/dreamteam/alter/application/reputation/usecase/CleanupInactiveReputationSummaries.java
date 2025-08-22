package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.inbound.CleanupInactiveReputationSummariesUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("cleanupInactiveReputationSummaries")
@RequiredArgsConstructor
@Transactional
public class CleanupInactiveReputationSummaries implements CleanupInactiveReputationSummariesUseCase {

    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;
    private final ReputationSummaryRepository reputationSummaryRepository;

    // 비활성 기준: 12개월 (1년)
    private static final int INACTIVE_MONTHS = 12;

    @Override
    public void execute() {
        log.info("Starting cleanup of inactive reputation summaries");
        
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusMonths(INACTIVE_MONTHS);
        
        // 12개월간 평판 활동이 없는 사용자 요약 제거
        List<ReputationSummary> inactiveUserSummaries = reputationSummaryQueryRepository
            .findInactiveSummaries(ReputationType.USER, inactiveThreshold);
        
        for (ReputationSummary summary : inactiveUserSummaries) {
            reputationSummaryRepository.delete(summary);
            log.debug("Deleted inactive reputation summary for user: {}", summary.getTargetId());
        }
        
        // 12개월간 평판 활동이 없는 업장 요약 제거
        List<ReputationSummary> inactiveWorkspaceSummaries = reputationSummaryQueryRepository
            .findInactiveSummaries(ReputationType.WORKSPACE, inactiveThreshold);
        
        for (ReputationSummary summary : inactiveWorkspaceSummaries) {
            reputationSummaryRepository.delete(summary);
            log.debug("Deleted inactive reputation summary for workspace: {}", summary.getTargetId());
        }
        
        log.info("Cleaned up {} inactive user summaries and {} inactive workspace summaries",
            inactiveUserSummaries.size(), inactiveWorkspaceSummaries.size());
    }
}
