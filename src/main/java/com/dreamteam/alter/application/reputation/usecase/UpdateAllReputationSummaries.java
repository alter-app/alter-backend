package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.port.inbound.UpdateAllReputationSummariesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("updateAllReputationSummaries")
@RequiredArgsConstructor
@Transactional
public class UpdateAllReputationSummaries implements UpdateAllReputationSummariesUseCase {

    private final AsyncReputationProcessor asyncProcessor;

    @Override
    public void execute() {
        log.info("평판 요약 배치 업데이트 시작");

        // 사용자 평판 요약 갱신
        CompletableFuture<Integer> userFuture = asyncProcessor.updateUserReputationSummariesAsync();

        // 업장 평판 요약 갱신
        CompletableFuture<Integer> workspaceFuture = asyncProcessor.updateWorkspaceReputationSummariesAsync();

        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(userFuture, workspaceFuture).join();

        // 결과 수집
        int userCount = userFuture.getNow(0);
        int workspaceCount = workspaceFuture.getNow(0);

        log.info("평판 요약 배치 업데이트 완료 - 사용자: {}, 업장: {}", userCount, workspaceCount);
    }
}
