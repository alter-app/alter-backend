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

import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
        log.info("비활성 평판 요약 정리 배치 작업 시작");

        LocalDateTime inactiveThreshold = LocalDateTime.now()
            .minusMonths(INACTIVE_MONTHS);

        // 사용자 요약 정리
        CompletableFuture<Integer> userCleanupFuture = cleanupUserSummariesAsync(inactiveThreshold);

        // 업장 요약 정리
        CompletableFuture<Integer> workspaceCleanupFuture = cleanupWorkspaceSummariesAsync(inactiveThreshold);

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(userCleanupFuture, workspaceCleanupFuture)
            .join();

        // 결과 수집
        int userDeleted = userCleanupFuture.getNow(0);
        int workspaceDeleted = workspaceCleanupFuture.getNow(0);

        log.info("비활성 평판 요약 정리 완료 - 사용자: {}, 업장: {}", userDeleted, workspaceDeleted);
    }

    /**
     * 사용자 요약 정리 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> cleanupUserSummariesAsync(LocalDateTime inactiveThreshold) {
        List<ReputationSummary> inactiveUserSummaries = reputationSummaryQueryRepository
            .findInactiveSummaries(ReputationType.USER, inactiveThreshold);

        if (inactiveUserSummaries.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 각 사용자 요약별로 비동기 삭제 처리
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = inactiveUserSummaries.stream()
            .map(summary -> CompletableFuture.runAsync(() -> {
                try {
                    reputationSummaryRepository.delete(summary);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.warn("사용자 {} 비활성 평판 요약 삭제 실패: {}", summary.getTargetId(), e.getMessage());
                }
            }))
            .toList();

        // 모든 삭제 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();

        if (errorCount.get() > 0) {
            log.warn(
                "사용자 비활성 평판 요약 정리 완료 - 성공: {}, 실패: {}",
                successCount.get(), errorCount.get()
            );
        }

        return CompletableFuture.completedFuture(successCount.get());
    }

    /**
     * 업장 요약 정리 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> cleanupWorkspaceSummariesAsync(LocalDateTime inactiveThreshold) {
        List<ReputationSummary> inactiveWorkspaceSummaries = reputationSummaryQueryRepository
            .findInactiveSummaries(ReputationType.WORKSPACE, inactiveThreshold);

        if (inactiveWorkspaceSummaries.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 각 업장 요약별로 비동기 삭제 처리
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = inactiveWorkspaceSummaries.stream()
            .map(summary -> CompletableFuture.runAsync(() -> {
                try {
                    reputationSummaryRepository.delete(summary);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.warn("업장 {} 비활성 평판 요약 삭제 실패: {}", summary.getTargetId(), e.getMessage());
                }
            }))
            .toList();

        // 모든 삭제 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();

        if (errorCount.get() > 0) {
            log.warn(
                "업장 비활성 평판 요약 정리 완료 - 성공: {}, 실패: {}",
                successCount.get(), errorCount.get()
            );
        }

        return CompletableFuture.completedFuture(successCount.get());
    }
}
