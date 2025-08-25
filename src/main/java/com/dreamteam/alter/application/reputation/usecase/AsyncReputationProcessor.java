package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.config.AsyncConfig;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReputationProcessor {

    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;
    private final ReputationSummaryRepository reputationSummaryRepository;
    private final GenerateReputationSummaryUseCase generateReputationSummaryUseCase;

    // 최근 활동 기준: 1일
    private static final int RECENT_ACTIVITY_DAYS = 1;
    // 비활성 기준: 12개월 (1년)
    private static final int INACTIVE_MONTHS = 12;

    /**
     * 사용자 평판 요약 갱신 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> updateUserReputationSummariesAsync() {
        List<Long> recentActiveUserIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.USER,
            LocalDateTime.now().minusDays(RECENT_ACTIVITY_DAYS)
        );

        if (recentActiveUserIds.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 배치 크기로 분할하여 처리
        List<List<Long>> batches = splitIntoBatches(recentActiveUserIds, AsyncConfig.BatchConfig.BATCH_SIZE);

        // 배치별로 비동기 처리
        List<CompletableFuture<Integer>> batchFutures = batches.stream()
            .map(this::processUserBatch)
            .toList();

        // 모든 배치 처리 완료 대기
        CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
            .join();

        // 총 처리된 수 계산
        int totalProcessed = batchFutures.stream()
            .mapToInt(future -> future.getNow(0))
            .sum();

        return CompletableFuture.completedFuture(totalProcessed);
    }

    /**
     * 업장 평판 요약 갱신 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> updateWorkspaceReputationSummariesAsync() {
        List<Long> recentActiveWorkspaceIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.WORKSPACE,
            LocalDateTime.now().minusDays(RECENT_ACTIVITY_DAYS)
        );

        if (recentActiveWorkspaceIds.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 배치 크기로 분할하여 처리
        List<List<Long>> batches = splitIntoBatches(recentActiveWorkspaceIds, AsyncConfig.BatchConfig.BATCH_SIZE);

        // 배치별로 비동기 처리
        List<CompletableFuture<Integer>> batchFutures = batches.stream()
            .map(this::processWorkspaceBatch)
            .toList();

        // 모든 배치 처리 완료 대기
        CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
            .join();

        // 총 처리된 수 계산
        int totalProcessed = batchFutures.stream()
            .mapToInt(future -> future.getNow(0))
            .sum();

        return CompletableFuture.completedFuture(totalProcessed);
    }

    /**
     * 사용자 요약 정리 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> cleanupUserSummariesAsync() {
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusMonths(INACTIVE_MONTHS);
        return cleanupUserSummariesAsync(inactiveThreshold);
    }

    /**
     * 업장 요약 정리 (비동기)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> cleanupWorkspaceSummariesAsync() {
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusMonths(INACTIVE_MONTHS);
        return cleanupWorkspaceSummariesAsync(inactiveThreshold);
    }

    /**
     * 사용자 요약 정리 (비동기) - 파라미터 포함
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
     * 업장 요약 정리 (비동기) - 파라미터 포함
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

    /**
     * 리스트를 배치 크기로 분할
     */
    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
            .mapToObj(i -> {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, list.size());
                return list.subList(start, end);
            })
            .toList();
    }

    /**
     * 사용자 배치 처리
     */
    private CompletableFuture<Integer> processUserBatch(List<Long> userIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                generateReputationSummaryUseCase.execute(ReputationType.USER, userIds);
                log.info("사용자 배치 처리 완료 - 대상 수: {}", userIds.size());
                return userIds.size();
            } catch (Exception e) {
                log.error("사용자 배치 처리 실패 - 대상 수: {}", userIds.size(), e);
                return 0;
            }
        });
    }

    /**
     * 업장 배치 처리
     */
    private CompletableFuture<Integer> processWorkspaceBatch(List<Long> workspaceIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                generateReputationSummaryUseCase.execute(ReputationType.WORKSPACE, workspaceIds);
                log.info("업장 배치 처리 완료 - 대상 수: {}", workspaceIds.size());
                return workspaceIds.size();
            } catch (Exception e) {
                log.error("업장 배치 처리 실패 - 대상 수: {}", workspaceIds.size(), e);
                return 0;
            }
        });
    }
}
