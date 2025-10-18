package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.domain.workspace.port.inbound.ExpireSubstituteRequestsUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.WorkspaceScheduleService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service("workspaceScheduleService")
@RequiredArgsConstructor
public class WorkspaceScheduleServiceImpl implements WorkspaceScheduleService {

    @Resource(name = "expireSubstituteRequests")
    private final ExpireSubstituteRequestsUseCase expireSubstituteRequests;

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "expireSubstituteRequests", lockAtMostFor = "5m")
    public void expireSubstituteRequests() {
        expireSubstituteRequests.execute();
    }
}
