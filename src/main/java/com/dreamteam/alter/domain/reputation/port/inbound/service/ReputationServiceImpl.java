package com.dreamteam.alter.domain.reputation.port.inbound.service;

import com.dreamteam.alter.domain.reputation.port.inbound.CleanupInactiveReputationSummariesUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.ExpireReputationRequestsUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.UpdateAllReputationSummariesUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service("reputationService")
@RequiredArgsConstructor
public class ReputationServiceImpl implements ReputationService {

    @Resource(name = "expireReputationRequests")
    private final ExpireReputationRequestsUseCase expireReputationRequests;
    
    @Resource(name = "updateAllReputationSummaries")
    private final UpdateAllReputationSummariesUseCase updateAllReputationSummaries;
    
    @Resource(name = "cleanupInactiveReputationSummaries")
    private final CleanupInactiveReputationSummariesUseCase cleanupInactiveReputationSummaries;

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "expireReputationRequests", lockAtMostFor = "5m")
    public void expireReputationRequests() {
        expireReputationRequests.execute();
    }

    /**
     * 평판 요약 갱신 작업
     */
    @Override
    @Scheduled(cron = "0 0 1 * * *")
    @SchedulerLock(name = "updateReputationSummaries", lockAtMostFor = "1h")
    public void updateReputationSummaries() {
        updateAllReputationSummaries.execute();
    }

    /**
     * 최근 미활동 사용자 평판 요약 정리 작업
     */
    @Override
    @Scheduled(cron = "0 0 2 * * *")
    @SchedulerLock(name = "cleanupInactiveReputationSummaries", lockAtMostFor = "10m")
    public void cleanupInactiveReputationSummaries() {
        cleanupInactiveReputationSummaries.execute();
    }

}
