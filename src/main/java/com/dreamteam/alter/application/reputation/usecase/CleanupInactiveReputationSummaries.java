package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.port.inbound.CleanupInactiveReputationSummariesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("cleanupInactiveReputationSummaries")
@RequiredArgsConstructor
@Transactional
public class CleanupInactiveReputationSummaries implements CleanupInactiveReputationSummariesUseCase {

    private final AsyncReputationProcessor asyncProcessor;

    @Override
    public void execute() {
        log.info("비활성 평판 요약 정리 배치 작업 시작");

        // 사용자 요약 정리
        CompletableFuture<Integer> userCleanupFuture = asyncProcessor.cleanupUserSummariesAsync();

        // 업장 요약 정리
        CompletableFuture<Integer> workspaceCleanupFuture = asyncProcessor.cleanupWorkspaceSummariesAsync();

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(userCleanupFuture, workspaceCleanupFuture)
            .join();

        // 결과 수집
        int userDeleted = userCleanupFuture.getNow(0);
        int workspaceDeleted = workspaceCleanupFuture.getNow(0);

        log.info("비활성 평판 요약 정리 완료 - 사용자: {}, 업장: {}", userDeleted, workspaceDeleted);
    }
}
