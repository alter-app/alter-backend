package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.UpdateAllReputationSummariesUseCase;

import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("updateAllReputationSummaries")
@RequiredArgsConstructor
@Transactional
public class UpdateAllReputationSummaries implements UpdateAllReputationSummariesUseCase {


    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;
    private final GenerateReputationSummaryUseCase generateReputationSummaryUseCase;

    // 최근 활동 기준: 1일
    private static final int RECENT_ACTIVITY_DAYS = 1;

    @Override
    public void execute() {
        log.info("Starting batch update for all reputation summaries");

        // 사용자 평판 요약 갱신
        updateUserReputationSummaries();

        // 업장 평판 요약 갱신
        updateWorkspaceReputationSummaries();

        log.info("Completed batch update for all reputation summaries");
    }

    /**
     * 사용자 평판 요약 갱신 (최근 1일간 평판을 받은 사용자만)
     */
    private void updateUserReputationSummaries() {
        List<Long> recentActiveUserIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.USER, 
            LocalDateTime.now().minusDays(RECENT_ACTIVITY_DAYS)
        );
        
        log.info("Found {} users with recent reputation activity (last {} days) for summary update", 
            recentActiveUserIds.size(), RECENT_ACTIVITY_DAYS);
        
        for (Long userId : recentActiveUserIds) {
            generateReputationSummaryUseCase.execute(ReputationType.USER, userId);
            log.debug("Updated reputation summary for recently active user: {}", userId);
        }
    }

    /**
     * 업장 평판 요약 갱신 (최근 1일간 평판을 받은 업장만)
     */
    private void updateWorkspaceReputationSummaries() {
        List<Long> recentActiveWorkspaceIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.WORKSPACE, 
            LocalDateTime.now().minusDays(RECENT_ACTIVITY_DAYS)
        );
        
        log.info("Found {} workspaces with recent reputation activity (last {} days) for summary update", 
            recentActiveWorkspaceIds.size(), RECENT_ACTIVITY_DAYS);
        
        for (Long workspaceId : recentActiveWorkspaceIds) {
            generateReputationSummaryUseCase.execute(ReputationType.WORKSPACE, workspaceId);
            log.debug("Updated reputation summary for recently active workspace: {}", workspaceId);
        }
    }
}
