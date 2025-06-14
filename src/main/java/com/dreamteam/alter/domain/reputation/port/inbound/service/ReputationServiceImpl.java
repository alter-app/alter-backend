package com.dreamteam.alter.domain.reputation.port.inbound.service;

import com.dreamteam.alter.domain.reputation.port.inbound.ExpireReputationRequestsUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("reputationService")
@RequiredArgsConstructor
public class ReputationServiceImpl implements ReputationService {

    @Resource(name = "expireReputationRequests")
    private final ExpireReputationRequestsUseCase expireReputationRequests;

    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "expireReputationRequests", lockAtMostFor = "5m")
    public void expireReputationRequests() {
        expireReputationRequests.execute();
    }

}
