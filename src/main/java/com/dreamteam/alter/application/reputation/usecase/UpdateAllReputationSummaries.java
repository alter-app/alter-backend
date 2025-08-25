package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryUseCase;
import com.dreamteam.alter.domain.reputation.port.inbound.UpdateAllReputationSummariesUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.scheduling.annotation.Async;
import com.dreamteam.alter.common.config.AsyncConfig.BatchConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

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
        log.info("평판 요약 배치 업데이트 시작");

        // 사용자 평판 요약 갱신
        CompletableFuture<Integer> userFuture = updateUserReputationSummariesAsync();

        // 업장 평판 요약 갱신
        CompletableFuture<Integer> workspaceFuture = updateWorkspaceReputationSummariesAsync();

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(userFuture, workspaceFuture).join();

        // 결과 수집
        int userCount = userFuture.getNow(0);
        int workspaceCount = workspaceFuture.getNow(0);

        log.info("평판 요약 배치 업데이트 완료 - 사용자: {}, 업장: {}", userCount, workspaceCount);
    }

    /**
     * 사용자 평판 요약 갱신 (최근 1일간 평판을 받은 사용자만)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> updateUserReputationSummariesAsync() {
        List<Long> recentActiveUserIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.USER,
            LocalDateTime.now()
                .minusDays(RECENT_ACTIVITY_DAYS)
        );

        if (recentActiveUserIds.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 배치 크기로 분할하여 처리
        List<List<Long>> batches = splitIntoBatches(recentActiveUserIds, BatchConfig.BATCH_SIZE);

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
     * 업장 평판 요약 갱신 (최근 1일간 평판을 받은 업장만)
     */
    @Async("batchTaskExecutor")
    public CompletableFuture<Integer> updateWorkspaceReputationSummariesAsync() {
        List<Long> recentActiveWorkspaceIds = reputationSummaryQueryRepository.getActiveReputationTargets(
            ReputationType.WORKSPACE,
            LocalDateTime.now()
                .minusDays(RECENT_ACTIVITY_DAYS)
        );

        if (recentActiveWorkspaceIds.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        // 배치 크기로 분할하여 처리
        List<List<Long>> batches = splitIntoBatches(recentActiveWorkspaceIds, BatchConfig.BATCH_SIZE);

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
