package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.CleanupOrphanFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("cleanupOrphanFiles")
@RequiredArgsConstructor
public class CleanupOrphanFiles implements CleanupOrphanFilesUseCase {

    private final FileQueryRepository fileQueryRepository;

    @Resource(name = "fileDeleteService")
    private final FileDeleteService fileDeleteService;

    @Override
    @Transactional
    public void execute() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<File> orphanFiles = fileQueryRepository.findOrphanFiles(threshold);

        int successCount = 0;
        int failureCount = 0;
        for (File file : orphanFiles) {
            try {
                fileDeleteService.delete(file);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to cleanup orphan file id={}, key={}", file.getId(), file.getStoredKey(), e);
                failureCount++;
            }
        }

        log.info("Orphan file cleanup completed. total={}, success={}, failed={}", orphanFiles.size(), successCount, failureCount);
    }
}
