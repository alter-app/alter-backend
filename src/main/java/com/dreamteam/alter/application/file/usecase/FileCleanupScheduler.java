package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.domain.file.port.inbound.CleanupOrphanFilesUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("fileScheduleService")
@RequiredArgsConstructor
public class FileCleanupScheduler {

    @Resource(name = "cleanupOrphanFiles")
    private final CleanupOrphanFilesUseCase cleanupOrphanFiles;

    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(name = "cleanupOrphanFiles", lockAtMostFor = "30m")
    public void cleanupOrphanFiles() {
        cleanupOrphanFiles.execute();
    }
}
