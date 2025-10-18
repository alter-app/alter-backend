package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.domain.workspace.port.inbound.ExpireSubstituteRequestsUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service("workspaceSchedulerService")
@RequiredArgsConstructor
public class WorkspaceSchedulerService {

    @Resource(name = "expireSubstituteRequests")
    private final ExpireSubstituteRequestsUseCase expireSubstituteRequests;

    /**
     * 대타 요청 만료 처리 작업
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "expireSubstituteRequests", lockAtMostFor = "5m")
    public void expireSubstituteRequests() {
        expireSubstituteRequests.execute();
    }
}
